package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceiros;
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
import java.util.List;
import java.util.Optional;

@Component("fiiSeleniumScraper")
public class FiiSeleniumScraperAdapter implements FiiDataScrapperPort {
    private static final Logger logger = LoggerFactory.getLogger(FiiSeleniumScraperAdapter.class);


    // Dentro de FiiSeleniumScraperAdapter.java

    @Override
    public Mono<FiiDadosFinanceiros> scrape(String ticker) {
        String urlCompleta = "https://investidor10.com.br/fiis/" + ticker;
        logger.info("Iniciando scraping com Selenium para a url {}", urlCompleta);

        return Mono.fromCallable(() -> {
            // --- Configuração ---
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-blink-features=AutomationControlled");
            ChromeDriver driver = new ChromeDriver(options);

            DevTools devTools = driver.getDevTools();
            devTools.createSession();
            devTools.send(Network.enable(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            ));

            // --- Preparação do Listener ---
            final List<String> urlsCapturadas = new ArrayList<>();
            final List<String> palavrasChave = List.of("dividendos/chart", "historico-indicadores", "cotacao/fii");
            devTools.addListener(Network.requestWillBeSent(), requestSent -> {
                String url = requestSent.getRequest().getUrl();
                if (palavrasChave.stream().anyMatch(url::contains)) {
                    logger.info(">> URL de API RELEVANTE CAPTURADA: {}", url);
                    if (!urlsCapturadas.contains(url)) {
                        urlsCapturadas.add(url);
                    }
                }
            });

            // --- Execução Simplificada ---
            try {
                // 1. Navega para a URL alvo com o listener já ativo.
                driver.get(urlCompleta);

                // 2. Espera um tempo fixo e curto. 3 segundos são suficientes
                // para capturar as chamadas de API que acontecem na carga inicial.
                Thread.sleep(3000);

                logger.info("Total de {} URLs de API relevantes capturadas.", urlsCapturadas.size());
                urlsCapturadas.forEach(url -> logger.info("URL Final: {}", url));

                // Futuro: Aqui você usará o WebClient para chamar essas URLs
                // e processar o JSON que elas retornam.

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrompida", e);
            } finally {
                devTools.close();
                driver.quit();
            }

            // Por enquanto, ainda retornamos um objeto vazio
            return new FiiDadosFinanceiros();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}

