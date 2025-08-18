package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.*;

import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.FiiApiConstants.*;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v138.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Component("fiiSeleniumScraper")
public class FiiSeleniumScraperAdapter implements FiiDataScrapperPort {

    private static final Logger logger = LoggerFactory.getLogger(FiiSeleniumScraperAdapter.class);

    private final FiiHeaderScraper fiiHeaderScraper;
    private final FiiApiScraper fiiApiScraper;
    private final FiiInternalIdScrapper fiiInternalIdScrapper;
    private final FiiInfoSobreScraper fiiInfoSobreScraper;
    private final FiiCardsScraper fiiCardsScraper;

    public FiiSeleniumScraperAdapter(FiiHeaderScraper fiiHeaderScraper, FiiApiScraper fiiApiScraper, FiiInternalIdScrapper fiiInternalIdScrapper, FiiInfoSobreScraper fiiInfoSobreScraper, FiiCardsScraper fiiCardsScraper) {
        this.fiiHeaderScraper = fiiHeaderScraper;
        this.fiiApiScraper = fiiApiScraper;
        this.fiiInternalIdScrapper = fiiInternalIdScrapper;
        this.fiiInfoSobreScraper = fiiInfoSobreScraper;
        this.fiiCardsScraper = fiiCardsScraper;

    }

    @Override
    public Mono<FiiDadosFinanceirosDTO> scrape(String ticker) {
        String urlCompleta = "https://investidor10.com.br/fiis/" + ticker;
        logger.info("Iniciando scraping com Selenium para a url {}", urlCompleta);

        // 1. Criação robusta do WebDriver com tratamento de erros abrangente
        Mono<ChromeDriver> driverMono = Mono.fromCallable(() -> {
            try {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless=new");
                options.addArguments("--window-size=1920,1080");
                options.addArguments("--disable-blink-features=AutomationControlled");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--disable-gpu");
                options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");
                
                return new ChromeDriver(options);
            } catch (org.openqa.selenium.WebDriverException e) {
                logger.error("Falha ao inicializar ChromeDriver para FII ticker {}: {}", ticker, e.getMessage());
                throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.WebDriverInitializationException(
                    "Falha ao inicializar ChromeDriver para FII scraping", e);
            } catch (Exception e) {
                logger.error("Erro inesperado na criação do WebDriver para FII ticker {}: {}", ticker, e.getMessage());
                throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingException(
                    "Erro inesperado na criação do WebDriver", ticker, urlCompleta, "DRIVER_INIT", e) {
                    @Override
                    public String getErrorCode() {
                        return "FII_WEBDRIVER_UNEXPECTED_ERROR";
                    }
                };
            }
        });

        // 2. Configuração robusta do DevTools com degradação graceful
        return driverMono.flatMap(driver -> {
                    DevTools devTools = null;
                    final Map<String, String> urlsMapeadas = new ConcurrentHashMap<>();
                    
                    try {
                        devTools = driver.getDevTools();
                        devTools.createSession();
                        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
                        
                        // Configurar listener de rede com tratamento de erros
                        devTools.addListener(Network.requestWillBeSent(), requestSent -> {
                            try {
                                String url = requestSent.getRequest().getUrl();
                                for (String chave : FiiApiConstants.TODAS_AS_CHAVES) {
                                    if (url.contains(chave)) {
                                        urlsMapeadas.putIfAbsent(chave, url);
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
                                 // Navegação robusta com tratamento de timeout
                                 try {
                                     driver.manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(45));
                                     driver.get(urlCompleta);
                                     
                                     // Aguardar carregamento completo
                                     org.openqa.selenium.support.ui.WebDriverWait wait = 
                                         new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15));
                                     wait.until(webDriver -> ((org.openqa.selenium.JavascriptExecutor) webDriver)
                                         .executeScript("return document.readyState").equals("complete"));
                                         
                                 } catch (org.openqa.selenium.TimeoutException e) {
                                     logger.warn("Timeout no carregamento da página {} para FII ticker {}", urlCompleta, ticker);
                                     throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingTimeoutException(
                                         ticker, urlCompleta, java.time.Duration.ofSeconds(45), "FII_PAGE_LOAD");
                                 } catch (org.openqa.selenium.WebDriverException e) {
                                     logger.error("Falha na navegação para {} (FII ticker {}): {}", urlCompleta, ticker, e.getMessage());
                                     throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingException(
                                         "Falha na navegação FII", ticker, urlCompleta, "FII_NAVIGATION", e) {
                                         @Override
                                         public String getErrorCode() {
                                             return "FII_NAVIGATION_FAILED";
                                         }
                                     };
                                 }
                                 
                                 // Aguardar captura de APIs com timeout inteligente
                                 try {
                                     int maxWaitTime = 8000; // 8 segundos máximo
                                     int checkInterval = 1000; // Verificar a cada 1 segundo
                                     int waited = 0;
                                     
                                     while (waited < maxWaitTime && urlsMapeadas.size() < FiiApiConstants.TODAS_AS_CHAVES.size()) {
                                         Thread.sleep(checkInterval);
                                         waited += checkInterval;
                                         
                                         // Log progresso da captura
                                         if (waited % 2000 == 0) {
                                             logger.debug("Captura de APIs para ticker {}: {}/{} URLs após {}ms", 
                                                         ticker, urlsMapeadas.size(), FiiApiConstants.TODAS_AS_CHAVES.size(), waited);
                                         }
                                     }
                                     
                                     logger.info("Captura finalizada para ticker {}: {}/{} URLs em {}ms", 
                                                ticker, urlsMapeadas.size(), FiiApiConstants.TODAS_AS_CHAVES.size(), waited);
                                                
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
                                     
                                     // Verificar elementos essenciais para FII
                                     if (tempDoc.select("div.name-ticker").isEmpty()) {
                                         logger.warn("Elemento 'div.name-ticker' não encontrado para FII ticker {}", ticker);
                                     }
                                     
                                 } catch (Exception e) {
                                     logger.error("Falha na validação HTML para FII ticker {}: {}", ticker, e.getMessage());
                                     throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.HtmlStructureException(
                                         ticker, urlCompleta, "Falha na validação HTML FII: " + e.getMessage(), e);
                                 }
                                 
                                 // Verificar se capturou pelo menos algumas URLs importantes
                                 if (urlsMapeadas.isEmpty()) {
                                     logger.warn("Nenhuma URL de API capturada para FII ticker {}", ticker);
                                     throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.NetworkCaptureException(
                                         ticker, "Falha na captura de APIs - nenhuma URL encontrada", 0, FiiApiConstants.TODAS_AS_CHAVES.size(), null);
                                 }

                                 // Empacotar os resultados para a próxima etapa
                                 return new ScrapeResult(html, urlsMapeadas);
                            })
                            // 3. doFinally garante que o driver será fechado, independentemente do que acontecer
                             .doFinally(signalType -> {
                                 logger.info("Finalizando a sessão do Selenium para FII ticker {}. Sinal: {}", ticker, signalType);
                                 
                                 // Fechar DevTools com segurança
                                 if (finalDevTools != null) {
                                     try {
                                         finalDevTools.close();
                                         logger.debug("DevTools fechado com sucesso para ticker {}", ticker);
                                     } catch (Exception e) {
                                         logger.warn("Erro ao fechar DevTools para ticker {}: {}", ticker, e.getMessage());
                                     }
                                 }
                                 
                                 // Fechar WebDriver com segurança
                                 try {
                                     driver.quit();
                                     logger.debug("WebDriver fechado com sucesso para ticker {}", ticker);
                                 } catch (Exception e) {
                                     logger.warn("Erro ao fechar WebDriver para ticker {}: {}", ticker, e.getMessage());
                                 }
                             });
                })
                // 4. Este flatMap é a segunda etapa reativa do nosso pipeline

                .flatMap(result -> {
                    Document doc = Jsoup.parse(result.html());

                    // Processar os dados síncronos do HTML
                    FiiInfoHeaderDTO infoHeader = fiiHeaderScraper.scrape(doc);
                    Integer internalId = fiiInternalIdScrapper.scrape(new ArrayList<>(result.urlsMapeadas().values()));
                    FiiInfoSobreDTO infoSobreDTO = fiiInfoSobreScraper.scrape(doc);
                    FiiInfoCardsDTO infoCardsDTO = fiiCardsScraper.scrape(doc);

                    // Disparar as chamadas assíncronas da API
                    Mono<FiiCotacaoDTO> cotacaoMono = result.findUrl(COTACAO)
                            .map(fiiApiScraper::fetchCotacao)
                            .orElse(Mono.just(new FiiCotacaoDTO(null, null)));

                    Mono<List<FiiDividendoDTO>> dividendosMono = result.findUrl(DIVIDENDOS)
                            .map(fiiApiScraper::fetchDividendos)
                            .orElse(Mono.just(Collections.emptyList()));

                    Mono<FiiIndicadorHistoricoDTO> historicoMono = result.findUrl(HISTORICO_INDICADORES)
                            .map(fiiApiScraper::fetchHistorico)
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
                })
                // Esses operadores se aplicam a toda a cadeia reativa
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
}
