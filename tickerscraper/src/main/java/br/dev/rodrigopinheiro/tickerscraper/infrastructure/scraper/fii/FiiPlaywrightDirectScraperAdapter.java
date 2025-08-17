package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.PlaywrightInitializer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiCotacaoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDividendoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiIndicadorHistoricoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoCardsDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoHeaderDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoSobreDTO;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitUntilState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.FiiApiConstants.*;
@Component("fiiPlaywrightDirectScraper")
public class FiiPlaywrightDirectScraperAdapter implements FiiDataScrapperPort {

    private static final Logger logger = LoggerFactory.getLogger(FiiPlaywrightDirectScraperAdapter.class);

    private final PlaywrightInitializer pwInit;                 // Browser singleton (abre/fecha no @PostConstruct/@PreDestroy)
    private final FiiSeleniumScraperAdapter seleniumFallback;  // Fallback quando Playwright falhar
    private final FiiHeaderScraper headerScraper;
    private final FiiInfoSobreScraper infoSobreScraper;
    private final FiiCardsScraper cardsScraper;
    private final FiiInternalIdScrapper internalIdScrapper;
    private final FiiApiScraper apiScraper;

    public FiiPlaywrightDirectScraperAdapter(
            PlaywrightInitializer pwInit,
            FiiSeleniumScraperAdapter seleniumFallback,
            FiiHeaderScraper headerScraper,
            FiiInfoSobreScraper infoSobreScraper,
            FiiCardsScraper cardsScraper,
            FiiInternalIdScrapper internalIdScrapper,
            FiiApiScraper apiScraper
    ) {
        this.pwInit = pwInit;
        this.seleniumFallback = seleniumFallback;
        this.headerScraper = headerScraper;
        this.infoSobreScraper = infoSobreScraper;
        this.cardsScraper = cardsScraper;
        this.internalIdScrapper = internalIdScrapper;
        this.apiScraper = apiScraper;
    }

    @Override
    @CircuitBreaker(name = "scraper", fallbackMethod = "fallbackToSelenium")
    @Retry(name = "scraper")
    public Mono<FiiDadosFinanceirosDTO> scrape(String ticker) {
        final String url = "https://investidor10.com.br/fiis/" + ticker;
        return executarComPlaywright(ticker, url);
    }
    
    /**
     * Método de fallback quando o Circuit Breaker está aberto ou há falhas.
     * Utiliza Selenium como alternativa ao Playwright para FIIs.
     */
    public Mono<FiiDadosFinanceirosDTO> fallbackToSelenium(String ticker, Exception ex) {
        logger.warn("Fallback FII para Selenium ativado. Ticker: {}, Causa: {}", 
                   ticker, ex.getClass().getSimpleName());
        return seleniumFallback.scrape(ticker);
    }

    private Mono<FiiDadosFinanceirosDTO> executarComPlaywright(String ticker, String url) {
        // refs para fechar com segurança em cancel/erro/sucesso
        AtomicReference<BrowserContext> ctxRef = new AtomicReference<>();
        AtomicReference<Page> pageRef = new AtomicReference<>();

        return Mono.fromCallable(() -> {
                    logger.info("Iniciando scraping Playwright: {}", url);

                    Browser browser = pwInit.getBrowser(); // singleton já inicializado

                    // Contexto por scrape → isolamento + "anti-bot" básico
                    Browser.NewContextOptions ctxOpts = new Browser.NewContextOptions()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                            .setViewportSize(1920, 1080)
                            .setLocale("pt-BR")
                            .setTimezoneId("America/Sao_Paulo")
                            .setExtraHTTPHeaders(Map.of(
                                    "Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7"
                            ));

                    BrowserContext ctx = browser.newContext(ctxOpts);
                    // disfarce leve
                    ctx.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

                    Page page = ctx.newPage();
                    page.setDefaultTimeout(15_000);

                    ctxRef.set(ctx);
                    pageRef.set(page);

                    // Navegação com tratamento de exceções
                    try {
                        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
                    } catch (TimeoutError e) {
                        throw new ScrapingTimeoutException(ticker, url, Duration.ofSeconds(15), "NAVIGATION");
                    } catch (Exception e) {
                        if (e.getMessage().contains("blocked") || e.getMessage().contains("captcha")) {
                            throw new AntiBotDetectedException(ticker, url, e.getMessage(), "Playwright");
                        }
                        throw new RuntimeException("Navigation error for " + ticker + ": " + e.getMessage(), e);
                    }

                    // Captura de XHR por substring (sem regex)
                    final Map<String, String> urlsMapeadas = new HashMap<>();
                    page.onRequest(req -> {
                        String u = req.url();
                        for (String chave : TODAS_AS_CHAVES) {
                            if (!urlsMapeadas.containsKey(chave) && u.contains(chave)) {
                                urlsMapeadas.put(chave, u);
                                logger.info("API capturada ({}): {}", chave, u);
                            }
                        }
                    });

                    // Navegação (sem NETWORKIDLE) + espera passiva por endpoints críticos
                    page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                    // Esperas curtas por cada endpoint (polling leve com timeout)
                    waitForKey(urlsMapeadas, HISTORICO_INDICADORES, 12_000);
                    waitForKey(urlsMapeadas, DIVIDENDOS,           12_000);
                    waitForKey(urlsMapeadas, COTACAO,               12_000);

                    // HTML para parsers existentes
                    String html = page.content();
                    Document doc = Jsoup.parse(html);

                    // Parsers (compatíveis com Selenium/Playwright)
                    FiiInfoHeaderDTO infoHeader = headerScraper.scrape(doc);
                    FiiInfoSobreDTO infoSobre  = infoSobreScraper.scrape(doc);
                    FiiInfoCardsDTO infoCards  = cardsScraper.scrape(doc);

                    // ID interno via URLs capturadas
                    Integer internalId = internalIdScrapper.scrape(new ArrayList<>(urlsMapeadas.values()));

                    // Monos das APIs com fallback seguro
                    Mono<FiiCotacaoDTO> cotacaoMono = Optional.ofNullable(urlsMapeadas.get(COTACAO))
                            .map(apiScraper::fetchCotacao)
                            .orElse(Mono.just(new FiiCotacaoDTO(null, null)));

                    Mono<List<FiiDividendoDTO>> dividendosMono = Optional.ofNullable(urlsMapeadas.get(DIVIDENDOS))
                            .map(apiScraper::fetchDividendos)
                            .orElse(Mono.just(Collections.emptyList()));

                    Mono<FiiIndicadorHistoricoDTO> historicoMono = Optional.ofNullable(urlsMapeadas.get(HISTORICO_INDICADORES))
                            .map(apiScraper::fetchHistorico)
                            .orElse(Mono.just(new FiiIndicadorHistoricoDTO(Collections.emptyMap())));

                    // Composição final
                    return Mono.zip(cotacaoMono, dividendosMono, historicoMono)
                            .map(t -> new FiiDadosFinanceirosDTO(
                                    internalId,
                                    infoHeader,
                                    t.getT3(), // historico
                                    infoSobre,
                                    infoCards,
                                    t.getT2(), // dividendos
                                    t.getT1()  // cotacao
                            ));
                })
                .flatMap(m -> m)
                .doOnError(e -> logger.error("Falha no Playwright para {}: {}", ticker, e.toString()))
                // limpeza segura SEMPRE
                .doOnCancel(() -> closeQuietly(pageRef.get(), ctxRef.get()))
                .doFinally(sig -> closeQuietly(pageRef.get(), ctxRef.get()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /** Espera passiva (polling leve) até uma chave aparecer no mapa, com timeout em ms. */
    private static void waitForKey(Map<String, String> map, String key, int timeoutMs) {
        long deadline = System.nanoTime() + timeoutMs * 1_000_000L;
        while (System.nanoTime() < deadline) {
            if (map.containsKey(key)) return;
            try { Thread.sleep(40); } catch (InterruptedException ignored) {}
        }
        // Timeout é aceitável — os Monos já têm fallback default
    }

    /** Fecha Page e Context sem propagar exceção. Não fecha Browser (singleton fecha no @PreDestroy). */
    private static void closeQuietly(Page page, BrowserContext ctx) {
        try { if (page != null) page.close(); } catch (Exception ignored) {}
        try { if (ctx != null) ctx.close(); } catch (Exception ignored) {}
    }
}
