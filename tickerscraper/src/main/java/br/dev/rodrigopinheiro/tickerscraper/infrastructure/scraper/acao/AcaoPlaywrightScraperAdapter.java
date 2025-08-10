package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.PlaywrightInitializer;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.*;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component("acaoPlaywrightScraper")
public class AcaoPlaywrightScraperAdapter implements AcaoDataScrapperPort {

    private static final Logger logger = LoggerFactory.getLogger(AcaoPlaywrightScraperAdapter.class);

    private final PlaywrightInitializer pwInit;                 // Browser singleton
    private final AcaoSeleniumScraperAdapter seleniumFallback; // fallback
    private final AcaoHeaderScraper headerScraper;
    private final AcaoCardsScraper cardsScraper;
    private final AcaoDetailedInfoScraper detailedInfoScraper;
    private final AcaoIndicatorsScraper indicatorsScraper;

    public AcaoPlaywrightScraperAdapter(
            PlaywrightInitializer pwInit,
            AcaoSeleniumScraperAdapter seleniumFallback,
            AcaoHeaderScraper headerScraper,
            AcaoCardsScraper cardsScraper,
            AcaoDetailedInfoScraper detailedInfoScraper,
            AcaoIndicatorsScraper indicatorsScraper
    ) {
        this.pwInit = pwInit;
        this.seleniumFallback = seleniumFallback;
        this.headerScraper = headerScraper;
        this.cardsScraper = cardsScraper;
        this.detailedInfoScraper = detailedInfoScraper;
        this.indicatorsScraper = indicatorsScraper;
    }

    @Override
    public Mono<AcaoDadosFinanceirosDTO> scrape(String ticker) {
        final String url = "https://investidor10.com.br/acao/" + ticker;

        return Mono.defer(() -> executarComPlaywright(ticker, url))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .onErrorResume(e -> {
                    logger.warn("Playwright (Ação) falhou para {}, fallback Selenium. Causa: {}", ticker, e.toString());
                    return seleniumFallback.scrape(ticker);
                });
    }

    private Mono<AcaoDadosFinanceirosDTO> executarComPlaywright(String ticker, String url) {
        AtomicReference<BrowserContext> ctxRef = new AtomicReference<>();
        AtomicReference<Page> pageRef = new AtomicReference<>();

        return Mono.fromCallable(() -> {
                    try (var ignored = MDC.putCloseable("ticker", ticker)) {
                        logger.info("Iniciando scraping Playwright (Ação): {}", url);
                        Browser browser = pwInit.getBrowser();

                        // Contexto por scrape + anti-bot básico
                        Browser.NewContextOptions ctxOpts = new Browser.NewContextOptions()
                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                                .setViewportSize(1920, 1080)
                                .setLocale("pt-BR")
                                .setTimezoneId("America/Sao_Paulo")
                                .setExtraHTTPHeaders(Map.of("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7"));

                        BrowserContext ctx = browser.newContext(ctxOpts);
                        ctx.addInitScript("Object.defineProperty(navigator,'webdriver',{get:()=>undefined})");

                        Page page = ctx.newPage();
                        page.setDefaultTimeout(15_000);

                        ctxRef.set(ctx);
                        pageRef.set(page);


                        // Navegar e esperar DOM básico
                        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                        // Esperar seletores que os scrapers usam (cada um com timeout curto)
                        waitSelectorQuiet(page, "div.name-ticker", 10_000);       // usado no header
                        waitSelectorQuiet(page, "section#cards-ticker", 10_000);  // usado nos cards
                        waitSelectorQuiet(page, "#table-indicators", 10_000);     // usado nos indicadores

                        // HTML final
                        String html = page.content();
                        Document doc = Jsoup.parse(html);

                        // Scrapers existentes (iguais ao Selenium)
                        AcaoInfoHeaderDTO header = headerScraper.scrapeInfoHeader(doc);
                        AcaoInfoCardsDTO cards = cardsScraper.scrapeCardsInfo(doc);
                        AcaoInfoDetailedDTO detailed = detailedInfoScraper.scrapeAndParseDetailedInfo(doc);
                        AcaoIndicadoresFundamentalistasDTO indicators = indicatorsScraper.scrape(doc, ticker);

                        AcaoDadosFinanceirosDTO dto = new AcaoDadosFinanceirosDTO(header, detailed, cards, indicators);
                        logger.info("Acao DTO montado para {}.", ticker);
                        return dto;
                    }
                })
                .doOnError(e -> logger.error("Falha Playwright (Ação) para {}: {}", ticker, e.toString()))
                .doOnCancel(() -> closeQuietly(pageRef.get(), ctxRef.get()))
                .doFinally(sig -> closeQuietly(pageRef.get(), ctxRef.get()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static void waitSelectorQuiet(Page page, String selector, int timeoutMs) {
        try {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
        } catch (Exception ignored) {
            // Se não aparecer, seguimos — scrapers podem lidar com vazio, e você mantém fallback Selenium no nível superior
        }
    }

    private static void closeQuietly(Page page, BrowserContext ctx) {
        try { if (page != null) page.close(); } catch (Exception ignored) {}
        try { if (ctx != null) ctx.close(); } catch (Exception ignored) {}
    }
}
