package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.PlaywrightInitializer;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base.AbstractScraperAdapter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.*;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

@Component("acaoPlaywrightScraper")
public class AcaoPlaywrightScraperAdapter extends AbstractScraperAdapter<AcaoDadosFinanceirosDTO> implements AcaoDataScrapperPort {

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
        final String url = buildUrl(ticker);
        return executarComPlaywright(ticker, url);
    }
    
    /**
     * Métod de fallback quando o Circuit Breaker está aberto ou há falhas.
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

        return createReactiveStructure(() -> {
            logger.info("Iniciando scraping Playwright (Ação): {}", url);
            Browser browser = pwInit.getBrowser();

            // Usar métodos da classe base para configuração
            BrowserContext ctx = createPlaywrightContext(browser);
            Page page = createPlaywrightPage(ctx);

            ctxRef.set(ctx);
            pageRef.set(page);

            // Navegar e validar usando métod da classe base
            navigateAndValidate(page, url, ticker);

            // Esperar seletores essenciais com fallbacks
            boolean hasEssentialElements = waitForAnySelector(page, ESSENTIAL_SELECTORS, 10_000, ticker, url);
            boolean hasCardsElements = waitForAnySelector(page, CARDS_SELECTORS, 10_000, ticker, url);
            boolean hasIndicatorsElements = waitForAnySelector(page, INDICATORS_SELECTORS, 10_000, ticker, url);
            
            // Validar elementos essenciais usando métod da classe base
            if (!hasEssentialElements && !hasCardsElements && !hasIndicatorsElements) {
                logger.error("Nenhum elemento essencial encontrado para ticker {} - possível ticker inexistente", ticker);
                
                // Verificar se a página contém indicadores de erro ou ticker não encontrado
                String html = page.content();
                if (html.contains("410 Gone") || html.contains("Not Found") || 
                    html.contains("Página não encontrada") || html.contains("Ticker não encontrado")) {
                    throw new TickerNotFoundException(ticker, url);
                }
                
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

            // HTML final e execução do scraping específico
            String html = page.content();
            Document doc = Jsoup.parse(html);
            
            return executeSpecificScraping(doc, ticker);
        }, ticker, () -> closePlaywrightResources(pageRef.get(), ctxRef.get()));
    }

    // Template methods implementation
    
    @Override
    protected String[] getEssentialSelectors() {
        return ESSENTIAL_SELECTORS;
    }
    
    @Override
    protected String[] getCardsSelectors() {
        return CARDS_SELECTORS;
    }
    
    @Override
    protected String buildUrl(String ticker) {
        return "https://investidor10.com.br/acoes/" + ticker;
    }
    
    @Override
    protected AcaoDadosFinanceirosDTO executeSpecificScraping(Document doc, String ticker) {
        // Scrapers existentes (iguais ao Selenium)
        AcaoInfoHeaderDTO header = headerScraper.scrapeInfoHeader(doc);
        AcaoInfoCardsDTO cards = cardsScraper.scrapeCardsInfo(doc);
        AcaoInfoDetailedDTO detailed = detailedInfoScraper.scrapeAndParseDetailedInfo(doc);
        AcaoIndicadoresFundamentalistasDTO indicators = indicatorsScraper.scrape(doc, ticker);

        AcaoDadosFinanceirosDTO dto = new AcaoDadosFinanceirosDTO(header, detailed, cards, indicators);
        logger.info("Acao DTO montado para {}.", ticker);
        return dto;
    }
}
