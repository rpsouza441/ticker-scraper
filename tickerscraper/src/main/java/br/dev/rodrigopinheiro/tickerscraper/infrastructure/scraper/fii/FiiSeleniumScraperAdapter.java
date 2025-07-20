package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;


import org.openqa.selenium.devtools.v138.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component("fiiSeleniumScraper")
public class FiiSeleniumScraperAdapter implements FiiDataScrapperPort {
    private static final Logger logger = LoggerFactory.getLogger(FiiSeleniumScraperAdapter.class);

    private final FiiHeaderScraper fiiHeaderScraper;

    public FiiSeleniumScraperAdapter(FiiHeaderScraper fiiHeaderScraper) {
        this.fiiHeaderScraper = fiiHeaderScraper;
    }


    // Dentro de FiiSeleniumScraperAdapter.java

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
            DevTools devTools = driver.getDevTools();
            devTools.createSession();

            //Preparacao do listener de Rede
            devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
            final List<String> urlsCapturadas = new CopyOnWriteArrayList<>(); // Lista segura para múltiplas threads
            final List<String> palavrasChave = List.of("dividendos/chart", "historico-indicadores", "cotacao/fii");

            devTools.addListener(Network.requestWillBeSent(), requestSent -> {
                String url = requestSent.getRequest().getUrl();
                if (palavrasChave.stream().anyMatch(url::contains)) {
                    if (!urlsCapturadas.contains(url)) {
                        urlsCapturadas.add(url);
                        logger.info(">> URL de API RELEVANTE CAPTURADA: {}", url);
                    }
                }
            });
            FiiDadosFinanceirosDTO resultadoFinal;
            try {
                // --- Interação "Humana" com a Página ---
                driver.get(urlCompleta);
                Thread.sleep(3000); // Espera simples para a página carregar

                // Pega o HTML final
                String html = driver.getPageSource();
                Document doc = Jsoup.parse(html);

                //  CHAMA O ESPECIALISTA EM CABEÇALHO
                FiiInfoHeaderDTO infoHeader = fiiHeaderScraper.scrape(doc);
                logger.info("Cabeçalho raspado com sucesso: {}", infoHeader);

                // Espera INTELIGENTE: Espera até que um elemento principal esteja visível.
                // Isso é mais confiável do que um sleep fixo.
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dividends")));
                logger.info("Página principal carregada. Iniciando rolagem...");

                // Rolagem INTELIGENTE: Rola até elementos específicos para disparar o lazy loading.
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("window.scrollTo(0, document.body.scrollHeight/2);"); // Rola até a metade
                Thread.sleep(1500); // Pausa para a rede responder
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");  // Rola até o fim
                Thread.sleep(2000); // Pausa final para capturar as últimas chamadas

                logger.info("Total de {} URLs de API relevantes capturadas.", urlsCapturadas.size());
                urlsCapturadas.forEach(url -> logger.info("URL Final: {}", url));

                // Por enquanto, criamos placeholders
                FiiIndicadorHistoricoDTO infoHistorico = new FiiIndicadorHistoricoDTO(Collections.emptyMap());
                FiiInfoSobreDTO infoSobre = new FiiInfoSobreDTO("Razão Social a ser Raspada", "CNPJ a ser Raspado", null, null, null, null, null, null, null, null, null, null, null, null, null);
                List<FiiDividendoDTO> dividendos = new ArrayList<>();
                FiiCotacaoDTO cotacao = new FiiCotacaoDTO(null, null);
                FiiInfoCardsDTO infoCards = new FiiInfoCardsDTO("", "");
                // Por enquanto, criamos placeholders
                resultadoFinal = new FiiDadosFinanceirosDTO(
                        infoHeader,
                        infoHistorico,
                        infoSobre,
                        infoCards,
                        dividendos,
                        cotacao
                );
            } catch (Exception e) {
                logger.error("Ocorreu um erro durante o scraping com Selenium.", e);
                throw new RuntimeException("Erro no processo de scraping", e);
            } finally {
                devTools.close();
                driver.quit();
            }


            // Retorna o DTO "esqueleto" preenchido com os placeholders
            return resultadoFinal;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}

