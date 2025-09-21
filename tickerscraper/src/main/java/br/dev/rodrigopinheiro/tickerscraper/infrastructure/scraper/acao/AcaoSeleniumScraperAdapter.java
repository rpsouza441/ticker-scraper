package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base.AbstractScraperAdapter;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.selenium.retry.SeleniumRetryManager;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v138.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Component("acaoSeleniumScraper")
public class AcaoSeleniumScraperAdapter extends AbstractScraperAdapter<AcaoDadosFinanceirosDTO> implements AcaoDataScrapperPort {
    private static final Logger logger = LoggerFactory.getLogger(AcaoSeleniumScraperAdapter.class);
    
    // Constantes para seletores CSS com fallbacks para validação de elementos essenciais
    private static final String[] ESSENTIAL_SELECTORS = {"div.name-ticker", "div.container-header", "header div.company-info"};
    private static final String[] CARDS_SELECTORS = {"section#cards-ticker", ".cards-section", ".ticker-cards"};
    private static final String[] INDICATORS_SELECTORS = {"#table-indicators", ".indicators-table", "table.indicators"};
    private final AcaoHeaderScraper acaoHeaderScraper;
    private final AcaoCardsScraper cardsScraper;
    private final AcaoDetailedInfoScraper detailedInfoScraper;
    private final AcaoIndicatorsScraper indicatorsScraper;
    private final SeleniumRetryManager retryManager;

    public AcaoSeleniumScraperAdapter(AcaoHeaderScraper acaoHeaderScraper,
                                      AcaoCardsScraper cardsScraper,
                                      AcaoDetailedInfoScraper detailedInfoScraper,
                                      AcaoIndicatorsScraper indicatorsScraper,
                                      SeleniumRetryManager retryManager) {
        this.acaoHeaderScraper = acaoHeaderScraper;
        this.cardsScraper = cardsScraper;
        this.detailedInfoScraper = detailedInfoScraper;
        this.indicatorsScraper = indicatorsScraper;
        this.retryManager = retryManager;
        
        logger.info("AcaoSeleniumScraperAdapter inicializado com retry manager: {}", 
                   retryManager.getRetryConfiguration());
    }

    @Override
    public Mono<AcaoDadosFinanceirosDTO> scrape(String ticker) {
        final String url = buildUrl(ticker);
        
        return createReactiveStructure(() -> {
            ChromeDriver driver = null;
            DevTools devTools = null;
            
            try {
                logger.info("Iniciando scraping Selenium para ação: {} - URL: {}", ticker, url);
                
                // Configurar WebDriverManager
                WebDriverManager.chromedriver().setup();
                
                // Configurar opções do Chrome
                ChromeOptions options = createChromeOptions();
                
                //  Inicializar driver com retry
                driver = retryManager.createWebDriverWithRetry(options);
                logger.debug("ChromeDriver inicializado com sucesso para ticker: {}", ticker);
                
                //  Configurar DevTools com retry
                devTools = retryManager.setupDevToolsWithRetry(driver);
                logger.debug("DevTools configurado com sucesso para ticker: {}", ticker);
                
                //  Navegar para a página com retry
                retryManager.loadPageWithRetry(driver, url);
                logger.debug("Página carregada com sucesso para ticker: {}", ticker);
                
                // Obter HTML da página
                String pageSource = driver.getPageSource();
                Document doc = Jsoup.parse(pageSource);
                
                // Validar elementos essenciais usando AbstractScraperAdapter
                validateEssentialElements(doc, ESSENTIAL_SELECTORS, CARDS_SELECTORS, ticker, url);
                logger.debug("Elementos essenciais validados para ticker: {}", ticker);
                
                // Executar scraping específico
                AcaoDadosFinanceirosDTO result = executeSpecificScraping(doc, ticker);
                logger.info("Scraping Selenium concluído com sucesso para ação: {}", ticker);
                
                return result;
                
            } catch (Exception e) {
                logger.error("Erro durante scraping Selenium para ação {}: {}", ticker, e.getMessage(), e);
                throw e;
            } finally {
                // Limpeza de recursos usando AbstractScraperAdapter
                closeSeleniumResources(driver, devTools);
                logger.debug("Recursos Selenium limpos para ticker: {}", ticker);
            }
        }, ticker, () -> {});
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
        AcaoInfoHeaderDTO header = acaoHeaderScraper.scrapeInfoHeader(doc);
        AcaoInfoCardsDTO cards = cardsScraper.scrapeCardsInfo(doc);
        AcaoInfoDetailedDTO detailed = detailedInfoScraper.scrapeAndParseDetailedInfo(doc);
        AcaoIndicadoresFundamentalistasDTO indicators = indicatorsScraper.scrape(doc, ticker);
        AcaoDadosFinanceirosDTO dadosFinanceiros = new AcaoDadosFinanceirosDTO(header, detailed, cards, indicators);
        logger.info("Dados financeiros de Acao {}", dadosFinanceiros.toString());
        return dadosFinanceiros;
    }
}