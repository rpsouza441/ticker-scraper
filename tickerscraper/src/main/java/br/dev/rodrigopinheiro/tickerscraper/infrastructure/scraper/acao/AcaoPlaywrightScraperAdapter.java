package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.PlaywrightInitializer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.*;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitUntilState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component("acaoPlaywrightScraper")
public class AcaoPlaywrightScraperAdapter implements AcaoDataScrapperPort {

    private static final Logger logger = LoggerFactory.getLogger(AcaoPlaywrightScraperAdapter.class);
    
    // Constantes para seletores CSS com fallbacks para validação de elementos essenciais
    private static final String[] ESSENTIAL_SELECTORS = {"div.name-ticker", "div.container-header", "header div.company-info"};
    private static final String[] CARDS_SELECTORS = {"section#cards-ticker", ".cards-section", ".ticker-cards"};
    private static final String[] INDICATORS_SELECTORS = {"#table-indicators", ".indicators-table", "table.indicators"};

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
    @CircuitBreaker(name = "scraper", fallbackMethod = "fallbackToSelenium")
    @Retry(name = "scraper")
    public Mono<AcaoDadosFinanceirosDTO> scrape(String ticker) {
        final String url = "https://investidor10.com.br/acao/" + ticker;
        return executarComPlaywright(ticker, url);
    }
    
    /**
     * Método de fallback quando o Circuit Breaker está aberto ou há falhas.
     * Utiliza Selenium como alternativa ao Playwright.
     */
    public Mono<AcaoDadosFinanceirosDTO> fallbackToSelenium(String ticker, Exception ex) {
        logger.warn("Fallback para Selenium ativado. Ticker: {}, Causa: {}", 
                   ticker, ex.getClass().getSimpleName());
        return seleniumFallback.scrape(ticker);
    }

    private Mono<AcaoDadosFinanceirosDTO> executarComPlaywright(String ticker, String url) {
        AtomicReference<BrowserContext> ctxRef = new AtomicReference<>();
        AtomicReference<Page> pageRef = new AtomicReference<>();

        return Mono.fromCallable(() -> {
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
                    try {
                        com.microsoft.playwright.Response response = page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
                        
                        // Verificar status HTTP para detectar ticker não encontrado
                        if (response != null) {
                            int status = response.status();
                            if (status == 404 || status == 410) {
                                logger.warn("Ticker {} não encontrado - HTTP {}: {}", ticker, status, response.statusText());
                                throw new TickerNotFoundException(ticker, url);
                            } else if (status >= 400) {
                                logger.warn("Erro HTTP {} para ticker {}: {}", status, ticker, response.statusText());
                                throw new ScrapingException("Erro HTTP " + status + ": " + response.statusText(), 
                                                           ticker, url, "HTTP_ERROR") {
                                    @Override
                                    public String getErrorCode() {
                                        return "HTTP_" + status;
                                    }
                                };
                            }
                        }
                    } catch (TimeoutError e) {
                        throw new ScrapingTimeoutException(ticker, url, Duration.ofSeconds(15), "NAVIGATION");
                    }

                    // Esperar seletores essenciais com fallbacks
                    boolean hasEssentialElements = waitForAnySelector(page, ESSENTIAL_SELECTORS, 10_000, ticker, url);
                    boolean hasCardsElements = waitForAnySelector(page, CARDS_SELECTORS, 10_000, ticker, url);
                    boolean hasIndicatorsElements = waitForAnySelector(page, INDICATORS_SELECTORS, 10_000, ticker, url);
                    
                    // Se nenhum elemento essencial foi encontrado, pode indicar ticker inexistente
                    if (!hasEssentialElements && !hasCardsElements && !hasIndicatorsElements) {
                        logger.error("Nenhum elemento essencial encontrado para ticker {} - possível ticker inexistente", ticker);
                        
                        // Verificar se a página contém indicadores de erro ou ticker não encontrado
                        String html = page.content();
                        if (html.contains("410 Gone") || html.contains("Not Found") || 
                            html.contains("Página não encontrada") || html.contains("Ticker não encontrado")) {
                            throw new TickerNotFoundException(ticker, url);
                        }
                        
                        // Se não há elementos essenciais mas não é claramente um erro 404/410,
                        // ainda assim pode ser um ticker inexistente
                        throw new TickerNotFoundException(ticker, url);
                    }
                    
                    if (!hasEssentialElements) {
                        logger.warn("Nenhum elemento essencial encontrado para ticker {} com seletores: {}", 
                                   ticker, java.util.Arrays.toString(ESSENTIAL_SELECTORS));
                    }
                    
                    if (!hasCardsElements) {
                        logger.warn("Nenhum elemento de cards encontrado para ticker {} com seletores: {}", 
                                   ticker, java.util.Arrays.toString(CARDS_SELECTORS));
                    }
                    
                    if (!hasIndicatorsElements) {
                        logger.warn("Nenhum elemento de indicadores encontrado para ticker {} com seletores: {}", 
                                   ticker, java.util.Arrays.toString(INDICATORS_SELECTORS));
                    }

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
                })
                .doOnError(e -> logger.error("Falha Playwright (Ação) para {}: {}", ticker, e.toString()))
                .doOnCancel(() -> closeQuietly(pageRef.get(), ctxRef.get()))
                .doFinally(sig -> closeQuietly(pageRef.get(), ctxRef.get()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static void waitSelectorWithException(Page page, String selector, int timeoutMs, String ticker, String url) {
        try {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
        } catch (TimeoutError e) {
            throw new DataParsingException(ticker, url, selector, "elemento HTML");
        } catch (Exception e) {
            // Outros erros podem indicar anti-bot
            if (e.getMessage().contains("blocked") || e.getMessage().contains("captcha")) {
                throw new AntiBotDetectedException(ticker, url, e.getMessage(), "Playwright");
            }
            throw new ScrapingException(e.getMessage(), ticker, url, "SELECTOR_WAIT", e) {
                @Override
                public String getErrorCode() {
                    return "SELECTOR_ERROR";
                }
            };
        }
    }
    
    /**
     * Espera por qualquer um dos seletores fornecidos, retornando true se algum for encontrado.
     */
    private static boolean waitForAnySelector(Page page, String[] selectors, int timeoutMs, String ticker, String url) {
        for (String selector : selectors) {
            try {
                page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
                logger.debug("Elemento encontrado com seletor: {} para ticker: {}", selector, ticker);
                return true;
            } catch (TimeoutError e) {
                // Continua tentando os próximos seletores
                continue;
            } catch (Exception e) {
                // Outros erros podem indicar anti-bot
                if (e.getMessage().contains("blocked") || e.getMessage().contains("captcha")) {
                    throw new AntiBotDetectedException(ticker, url, e.getMessage(), "Playwright");
                }
                // Para outros erros, continua tentando
                continue;
            }
        }
        return false; // Nenhum seletor foi encontrado
    }
    
    private static void closeQuietly(Page page, BrowserContext ctx) {
        try { if (page != null) page.close(); } catch (Exception ignored) {}
        try { if (ctx != null) ctx.close(); } catch (Exception ignored) {}
    }
}
