package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.*;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.FiiApiConstants.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


import org.openqa.selenium.devtools.v138.network.Network;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component("fiiSeleniumScraper")
public class FiiSeleniumScraperAdapter implements FiiDataScrapperPort {
    private static final Logger logger = LoggerFactory.getLogger(FiiSeleniumScraperAdapter.class);

    private final FiiInternalIdScrapper fiiInternalIdScrapper;
    private final FiiHeaderScraper fiiHeaderScraper;
    private final FiiApiScraper fiiApiScraper;

    public FiiSeleniumScraperAdapter(FiiInternalIdScrapper fiiInternalIdScrapper, FiiHeaderScraper fiiHeaderScraper, FiiApiScraper fiiApiScraper) {
        this.fiiInternalIdScrapper = fiiInternalIdScrapper;
        this.fiiHeaderScraper = fiiHeaderScraper;
        this.fiiApiScraper = fiiApiScraper;
    }
    @Override
    public Mono<FiiDadosFinanceirosDTO> scrape(String ticker) {
        String urlCompleta = "https://investidor10.com.br/fiis/" + ticker;
        logger.info("Iniciando scraping com Selenium para a url {}", urlCompleta);

        return Mono.fromCallable(() -> {
            // --- Configuração ---
            ChromeOptions options = new ChromeOptions();

            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");

            ChromeDriver driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            //WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            DevTools devTools = driver.getDevTools();
            devTools.createSession();

            final Map<String, String> urlsMapeadas = new ConcurrentHashMap<>();
            devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
            devTools.addListener(Network.requestWillBeSent(), requestSent -> {
                String url = requestSent.getRequest().getUrl();
                for (String chave : FiiApiConstants.TODAS_AS_CHAVES) {
                    if (url.contains(chave)) {
                        urlsMapeadas.putIfAbsent(chave, url);
                        logger.info(">> URL de API do tipo '{}' CAPTURADA: {}", chave, url);
                        break;
                    }
                }
            });


            String html;
                    try {
                        driver.get(urlCompleta);
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dividends-section")));
                        logger.info("Seção de dividendos carregada. As chamadas de API devem ter sido capturadas.");

                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        js.executeScript("window.scrollTo(0, document.body.scrollHeight/2);");
                        Thread.sleep(1000); // Pequena pausa para a rede responder após a rolagem final.

                        html = driver.getPageSource();
                        logger.info("Total de {} URLs de API relevantes capturadas.", urlsMapeadas.size());
                    } catch (Exception e) {
                        logger.error("Ocorreu um erro durante a fase de scraping com Selenium.", e);
                        throw new RuntimeException("Erro no processo de scraping com Selenium", e);
                    } finally {
                        devTools.close();
                        driver.quit();
                    }

                    return new ScrapeResult(html, urlsMapeadas);
                }).subscribeOn(Schedulers.boundedElastic())

                // Stage 2: The Reactive Work
                .flatMap(result -> {
                    Document doc = Jsoup.parse(result.html());

                    FiiInfoHeaderDTO infoHeader = fiiHeaderScraper.scrape(doc);
                    List<String> listaDeUrls = new ArrayList<>(result.urlsMapeadas().values());
                    Integer internalId = fiiInternalIdScrapper.scrape(listaDeUrls);

                    Mono<FiiCotacaoDTO> cotacaoMono = result.findUrl(COTACAO)
                            .map(fiiApiScraper::fetchCotacao)
                            .orElse(Mono.just(new FiiCotacaoDTO(null, null)));

                    Mono<List<FiiDividendoDTO>> dividendosMono = result.findUrl(DIVIDENDOS)
                            .map(fiiApiScraper::fetchDividendos)
                            .orElse(Mono.just(Collections.emptyList()));

                    Mono<FiiIndicadorHistoricoDTO> historicoMono = result.findUrl(HISTORICO_INDICADORES)
                            .map(fiiApiScraper::fetchHistorico)
                            .orElse(Mono.just(new FiiIndicadorHistoricoDTO(Collections.emptyMap())));

                    return Mono.zip(cotacaoMono, dividendosMono, historicoMono)
                            .map(tuple -> {
                                FiiCotacaoDTO cotacao = tuple.getT1();
                                List<FiiDividendoDTO> dividendos = tuple.getT2();
                                FiiIndicadorHistoricoDTO historico = tuple.getT3();

                                // Monta o DTO final com os dados REAIS, não com as promessas
                                return new FiiDadosFinanceirosDTO(
                                        internalId,
                                        infoHeader,
                                        historico,
                                        new FiiInfoSobreDTO(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null), // Placeholder
                                        new FiiInfoCardsDTO(null, null), // Placeholder
                                        dividendos,
                                        cotacao
                                );
                            });
                });
    }
}

