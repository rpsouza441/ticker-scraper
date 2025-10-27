package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.FiiApiConstants.COTACAO;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.FiiApiConstants.DIVIDENDOS;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.FiiApiConstants.HISTORICO_INDICADORES;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v138.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base.AbstractScraperAdapter;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.CapturedRequest;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiCotacaoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDividendoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiIndicadorHistoricoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoCardsDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoHeaderDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoSobreDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.ScrapeResult;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.selenium.retry.SeleniumRetryManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Component("fiiSeleniumScraper")
public class FiiSeleniumScraperAdapter extends AbstractScraperAdapter<FiiDadosFinanceirosDTO> implements FiiDataScrapperPort {

    private static final Logger logger = LoggerFactory.getLogger(FiiSeleniumScraperAdapter.class);
    
    // Constantes para seletores CSS com fallbacks para validação de elementos essenciais
    private static final String[] ESSENTIAL_SELECTORS = {"div.name-ticker", "div.container-header", "header div.fii-info"};
    private static final String[] CARDS_SELECTORS = {"section#cards-ticker", ".cards-section", ".fii-cards"};
    private static final String[] ABOUT_SELECTORS = {"div#about-company", "div.about-section", ".fii-about"};

    private final FiiHeaderScraper fiiHeaderScraper;
    private final FiiApiScraper fiiApiScraper;
    private final FiiInternalIdScrapper fiiInternalIdScrapper;
    private final FiiInfoSobreScraper fiiInfoSobreScraper;
    private final FiiCardsScraper fiiCardsScraper;
    private final SeleniumRetryManager retryManager;

    public FiiSeleniumScraperAdapter(FiiHeaderScraper fiiHeaderScraper, 
                                   FiiApiScraper fiiApiScraper, 
                                   FiiInternalIdScrapper fiiInternalIdScrapper, 
                                   FiiInfoSobreScraper fiiInfoSobreScraper, 
                                   FiiCardsScraper fiiCardsScraper,
                                   SeleniumRetryManager retryManager) {
        this.fiiHeaderScraper = fiiHeaderScraper;
        this.fiiApiScraper = fiiApiScraper;
        this.fiiInternalIdScrapper = fiiInternalIdScrapper;
        this.fiiInfoSobreScraper = fiiInfoSobreScraper;
        this.fiiCardsScraper = fiiCardsScraper;
        this.retryManager = retryManager;
        
        logger.info("FiiSeleniumScraperAdapter inicializado com retry manager: {}", 
                   retryManager.getRetryConfiguration());
    }

    @Override
    public Mono<FiiDadosFinanceirosDTO> scrape(String ticker) {
        String urlCompleta = buildUrl(ticker);
        logger.info("Iniciando scraping com Selenium para a url {}", urlCompleta);

        // 1. Criação robusta do WebDriver com retry automático
        Mono<ChromeDriver> driverMono = Mono.fromCallable(() -> {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = createChromeOptions();
            return retryManager.createWebDriverWithRetry(options);
        });

        // 2. Configuração robusta do DevTools com degradação graceful
        return driverMono.flatMap(driver -> {
                    DevTools devTools = null;
                    final Map<String, CapturedRequest> requestsMapeadas = new ConcurrentHashMap<>();
                    
                    try {
                        devTools = retryManager.setupDevToolsWithRetry(driver);
                        
                        // Configurar listener de rede com tratamento de erros
                        devTools.addListener(Network.requestWillBeSent(), requestSent -> {
                            try {
                                String url = requestSent.getRequest().getUrl();
                                Map<String, String> headers = requestSent.getRequest().getHeaders().entrySet().stream()
                                        .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
                                for (String chave : FiiApiConstants.TODAS_AS_CHAVES) {
                                    if (url.contains(chave)) {
                                        requestsMapeadas.putIfAbsent(chave, new CapturedRequest(url, headers));
                                        logger.info(">> URL de API do tipo '{}' CAPTURADA: {}", chave, url);
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                logger.warn("Erro ao processar requisição de rede para ticker {}: {}", ticker, e.getMessage());
                            }
                        });
                        
                        logger.info("DevTools configurado com sucesso para ticker {}", ticker);
                    } catch (Exception e) {
                        logger.warn("Falha ao configurar DevTools para ticker {}, continuando sem captura de rede: {}", 
                                   ticker, e.getMessage());
                        // Continua sem DevTools - degradação graceful
                    }
                    
                    final DevTools finalDevTools = devTools;

                    // A lógica principal de scraping está encapsulada em um novo Mono.fromCallable
                     return Mono.fromCallable(() -> {
                                 // Navegação robusta com retry automático
                                 retryManager.loadPageWithRetry(driver, urlCompleta);
                                 
                                 // Aguardar captura de APIs com timeout inteligente
                                 try {
                                     int maxWaitTime = 8000; // 8 segundos máximo
                                     int checkInterval = 1000; // Verificar a cada 1 segundo
                                     int waited = 0;
                                     
                                     while (waited < maxWaitTime && requestsMapeadas.size() < FiiApiConstants.TODAS_AS_CHAVES.size()) {
                                         Thread.sleep(checkInterval);
                                         waited += checkInterval;
                                         
                                         // Log progresso da captura
                                         if (waited % 2000 == 0) {
                                             logger.debug("Captura de APIs para ticker {}: {}/{} URLs após {}ms",
                                                         ticker, requestsMapeadas.size(), FiiApiConstants.TODAS_AS_CHAVES.size(), waited);
                                         }
                                     }
                                     
                                     logger.info("Captura finalizada para ticker {}: {}/{} URLs em {}ms",
                                                ticker, requestsMapeadas.size(), FiiApiConstants.TODAS_AS_CHAVES.size(), waited);
                                                
                                 } catch (InterruptedException e) {
                                     Thread.currentThread().interrupt();
                                     logger.warn("Captura de APIs interrompida para ticker {}", ticker);
                                 }
                                 
                                 // Validação e parsing do HTML
                                 String html;
                                 try {
                                     html = driver.getPageSource();
                                     if (html == null || html.trim().isEmpty()) {
                                         throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.HtmlStructureException(
                                             ticker, urlCompleta, "HTML vazio retornado pelo WebDriver para FII");
                                     }
                                     logger.info("HTML obtido com sucesso para FII ticker {}: {} caracteres", ticker, html.length());
                                 } catch (org.openqa.selenium.WebDriverException e) {
                                     logger.error("Falha ao obter HTML da página {} (FII ticker {}): {}", urlCompleta, ticker, e.getMessage());
                                     throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.HtmlStructureException(
                                         ticker, urlCompleta, "Falha ao obter HTML FII: " + e.getMessage(), e);
                                 }
                                 
                                 // Validação básica de estrutura HTML
                                 try {
                                     org.jsoup.nodes.Document tempDoc = Jsoup.parse(html);
                                     
                                     if (tempDoc.select("title").isEmpty()) {
                                         throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.HtmlStructureException(
                                             ticker, urlCompleta, "Estrutura HTML inválida para FII - sem título");
                                     }
                                     
                                     // Validar elementos essenciais usando método da classe base
                                 validateEssentialElements(tempDoc, ESSENTIAL_SELECTORS, CARDS_SELECTORS, ticker, urlCompleta);
                                     
                                 } catch (Exception e) {
                                     logger.error("Falha na validação HTML para FII ticker {}: {}", ticker, e.getMessage());
                                     throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.HtmlStructureException(
                                         ticker, urlCompleta, "Falha na validação HTML FII: " + e.getMessage(), e);
                                 }
                                 
                                 // Verificar se capturou pelo menos algumas URLs importantes
                                 if (requestsMapeadas.isEmpty()) {
                                     logger.warn("Nenhuma URL de API capturada para FII ticker {}", ticker);
                                     throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.NetworkCaptureException(
                                         ticker, "Falha na captura de APIs - nenhuma URL encontrada", 0, FiiApiConstants.TODAS_AS_CHAVES.size(), null);
                                 }

                                 // Empacotar os resultados para a próxima etapa
                                 return new ScrapeResult(html, requestsMapeadas);
                            })
                            // 3. doFinally garante que o driver será fechado, independentemente do que acontecer
                             .doFinally(signalType -> {
                                 logger.info("Finalizando a sessão do Selenium para FII ticker {}. Sinal: {}", ticker, signalType);
                                 
                                 // Fechar recursos usando método da classe base
                                 closeSeleniumResources(driver, finalDevTools);
                             });
                })
                // 4. Este flatMap é a segunda etapa reativa do nosso pipeline

                .flatMap(result -> {
                    Document doc = Jsoup.parse(result.html());

                    // Processar os dados síncronos do HTML
                    FiiInfoHeaderDTO infoHeader = fiiHeaderScraper.scrape(doc);
                    Integer internalId = fiiInternalIdScrapper.scrape(
                            result.requestsMapeadas().values().stream().map(CapturedRequest::url).toList());
                    FiiInfoSobreDTO infoSobreDTO = fiiInfoSobreScraper.scrape(doc);
                    FiiInfoCardsDTO infoCardsDTO = fiiCardsScraper.scrape(doc);

                    // Disparar as chamadas assíncronas da API
                    Mono<FiiCotacaoDTO> cotacaoMono = result.findRequest(COTACAO)
                            .map(req -> fiiApiScraper.fetchCotacao(req.url(), req.headers()))
                            .orElse(Mono.just(new FiiCotacaoDTO(null, null)));

                    Mono<List<FiiDividendoDTO>> dividendosMono = result.findRequest(DIVIDENDOS)
                            .map(req -> fiiApiScraper.fetchDividendos(req.url(), req.headers()))
                            .orElse(Mono.just(Collections.emptyList()));

                    Mono<FiiIndicadorHistoricoDTO> historicoMono = result.findRequest(HISTORICO_INDICADORES)
                            .map(req -> fiiApiScraper.fetchHistorico(req.url(), req.headers()))
                            .orElse(Mono.just(new FiiIndicadorHistoricoDTO(Collections.emptyMap())));

                    // 5. Usar Mono.zip para aguardar a conclusão de todas as chamadas da API e então combinar os resultados
                    return Mono.zip(cotacaoMono, dividendosMono, historicoMono)
                            .map(tuple -> {
                                FiiCotacaoDTO cotacao = tuple.getT1();
                                List<FiiDividendoDTO> dividendos = tuple.getT2();
                                FiiIndicadorHistoricoDTO historico = tuple.getT3();

                                // Montar o DTO final com todos os dados coletados

                                return new FiiDadosFinanceirosDTO(
                                        internalId,
                                        infoHeader,
                                        historico,
                                        infoSobreDTO,
                                        infoCardsDTO,
                                        dividendos,
                                        cotacao
                                );
                            });
                })// Esses operadores se aplicam a toda a cadeia reativa
                .doOnError(error -> {
                    if (error instanceof br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingException) {
                        logger.error("Falha específica no scraping FII para ticker {}: {} [{}]", 
                                   ticker, error.getMessage(), error.getClass().getSimpleName());
                    } else {
                        logger.error("Falha inesperada no FiiSeleniumScraperAdapter para ticker {}: {}", 
                                   ticker, error.getMessage(), error);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic()); // Ensures the entire operation runs on a dedicated thread
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
        return "https://investidor10.com.br/fiis/" + ticker;
    }
    
    @Override
    protected FiiDadosFinanceirosDTO executeSpecificScraping(Document doc, String ticker) {
        // Este método não é usado diretamente no FII pois há lógica específica de APIs
        // Mantido para compatibilidade com a classe abstrata
        throw new UnsupportedOperationException("FII scraping usa lógica específica com APIs");
    }
}