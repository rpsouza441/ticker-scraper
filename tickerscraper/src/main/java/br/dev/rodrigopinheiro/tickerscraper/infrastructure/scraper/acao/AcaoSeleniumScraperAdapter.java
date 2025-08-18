package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component("acaoSeleniumScraper")
public class AcaoSeleniumScraperAdapter implements AcaoDataScrapperPort {
    private static final Logger logger = LoggerFactory.getLogger(AcaoSeleniumScraperAdapter.class);
    private final AcaoHeaderScraper acaoHeaderScraper;
    private final AcaoCardsScraper cardsScraper;
    private final AcaoDetailedInfoScraper detailedInfoScraper;
    private final AcaoIndicatorsScraper indicatorsScraper;

    public AcaoSeleniumScraperAdapter(AcaoHeaderScraper acaoHeaderScraper,
                                      AcaoCardsScraper cardsScraper,
                                      AcaoDetailedInfoScraper detailedInfoScraper,
                                      AcaoIndicatorsScraper indicatorsScraper) {
        this.acaoHeaderScraper = acaoHeaderScraper;
        this.cardsScraper = cardsScraper;
        this.detailedInfoScraper = detailedInfoScraper;
        this.indicatorsScraper = indicatorsScraper;
    }

    @Override
    public Mono<AcaoDadosFinanceirosDTO> scrape(String ticker) {
        String urlCompleta = "https://investidor10.com.br/acoes/" + ticker;
        logger.info("Iniciando scraping com Selenium para a url {}", urlCompleta);

        // 1. Criamos um Mono que representa a "missão" de ter um WebDriver pronto com tratamento robusto.
        Mono<WebDriver> driverMono = Mono.fromCallable(() -> {
            try {
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
                logger.error("Falha ao inicializar ChromeDriver para ticker {}: {}", ticker, e.getMessage());
                throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.WebDriverInitializationException(
                    "Falha ao inicializar ChromeDriver", e);
            } catch (Exception e) {
                logger.error("Erro inesperado na criação do WebDriver para ticker {}: {}", ticker, e.getMessage());
                throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingException(
                    "Erro inesperado na criação do WebDriver", ticker, urlCompleta, "DRIVER_INIT", e) {
                    @Override
                    public String getErrorCode() {
                        return "WEBDRIVER_UNEXPECTED_ERROR";
                    }
                };
            }
        });

        return driverMono.flatMap(driver -> {
                    // 2. flatMap: Quando o driver estiver pronto, execute a lógica de scraping.
                    return Mono.fromCallable(() -> {
                                // Navegação com tratamento robusto
                                try {
                                    driver.manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(30));
                                    driver.get(urlCompleta);
                                    
                                    // Aguardar carregamento completo
                                    org.openqa.selenium.support.ui.WebDriverWait wait = 
                                        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
                                    wait.until(webDriver -> ((org.openqa.selenium.JavascriptExecutor) webDriver)
                                        .executeScript("return document.readyState").equals("complete"));
                                        
                                } catch (org.openqa.selenium.TimeoutException e) {
                                    logger.warn("Timeout no carregamento da página {} para ticker {}", urlCompleta, ticker);
                                    throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingTimeoutException(
                                        ticker, urlCompleta, java.time.Duration.ofSeconds(30), "PAGE_LOAD");
                                } catch (org.openqa.selenium.WebDriverException e) {
                                    logger.error("Falha na navegação para {} (ticker {}): {}", urlCompleta, ticker, e.getMessage());
                                    throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingException(
                                        "Falha na navegação", ticker, urlCompleta, "NAVIGATION", e) {
                                        @Override
                                        public String getErrorCode() {
                                            return "NAVIGATION_FAILED";
                                        }
                                    };
                                }
                                
                                // Validação e parsing do HTML
                                String html;
                                try {
                                    html = driver.getPageSource();
                                    if (html == null || html.trim().isEmpty()) {
                                        throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.HtmlStructureException(
                                            ticker, urlCompleta, "HTML vazio retornado pelo WebDriver");
                                    }
                                    logger.info("Selenium obteve HTML de {} caracteres para ticker {}.", html.length(), ticker);
                                } catch (org.openqa.selenium.WebDriverException e) {
                                    logger.error("Falha ao obter HTML da página {} (ticker {}): {}", urlCompleta, ticker, e.getMessage());
                                    throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.HtmlStructureException(
                                        ticker, urlCompleta, "Falha ao obter HTML: " + e.getMessage(), e);
                                }
                                
                                Document doc;
                                try {
                                    doc = Jsoup.parse(html);
                                    
                                    // Validação básica de estrutura
                                    if (doc.select("title").isEmpty()) {
                                        throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.HtmlStructureException(
                                            ticker, urlCompleta, "Estrutura HTML inválida - sem título");
                                    }
                                    
                                    // Verificar se elementos essenciais estão presentes
                                    if (doc.select("div.name-ticker").isEmpty()) {
                                        logger.warn("Elemento 'div.name-ticker' não encontrado para ticker {}", ticker);
                                    }
                                    
                                } catch (Exception e) {
                                    logger.error("Falha no parsing HTML para ticker {}: {}", ticker, e.getMessage());
                                    throw new br.dev.rodrigopinheiro.tickerscraper.domain.exception.HtmlStructureException(
                                        ticker, urlCompleta, "Falha no parsing HTML: " + e.getMessage(), e);
                                }

                                AcaoInfoHeaderDTO header = acaoHeaderScraper.scrapeInfoHeader(doc);
                                AcaoInfoCardsDTO cards = cardsScraper.scrapeCardsInfo(doc);
                                AcaoInfoDetailedDTO detailed = detailedInfoScraper.scrapeAndParseDetailedInfo(doc);
                                AcaoIndicadoresFundamentalistasDTO indicators = indicatorsScraper.scrape(doc, ticker);
                                AcaoDadosFinanceirosDTO dadosFinanceiros = new AcaoDadosFinanceirosDTO(header, detailed, cards, indicators);
                                logger.info("Dados financeiros de Acao {}", dadosFinanceiros.toString());
                                return dadosFinanceiros;

                            })// 3. doFinally: Garante que, aconteça o que acontecer (sucesso ou erro),
                            // a limpeza será executada. É o equivalente reativo do `finally`.
                            .doFinally(signalType -> {
                                logger.info("Finalizando a sessão do Selenium para {}. Sinal: {}", ticker, signalType);
                                driver.quit();
                            });
                })
                // 4. doOnError: Adiciona um "espião" para logar qualquer erro que ocorra no fluxo.
                .doOnError(error -> logger.error("O Mono do AcaoSeleniumScraperAdapter falhou para o ticker {}", ticker, error))
                // 5. subscribeOn: Garante que TODA a operação (desde criar o driver até o final)
                // rode em uma thread segura.
                .subscribeOn(Schedulers.boundedElastic());


    }
}