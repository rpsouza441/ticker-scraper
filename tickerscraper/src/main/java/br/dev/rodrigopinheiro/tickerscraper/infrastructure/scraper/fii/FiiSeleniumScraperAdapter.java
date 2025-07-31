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
    // TODO: Inject other scrapers like FiiInfoSobreScraper and FiiCardsScraper here

    public FiiSeleniumScraperAdapter(FiiHeaderScraper fiiHeaderScraper, FiiApiScraper fiiApiScraper, FiiInternalIdScrapper fiiInternalIdScrapper) {
        this.fiiHeaderScraper = fiiHeaderScraper;
        this.fiiApiScraper = fiiApiScraper;
        this.fiiInternalIdScrapper = fiiInternalIdScrapper;
    }

    @Override
    public Mono<FiiDadosFinanceirosDTO> scrape(String ticker) {
        String urlCompleta = "https://investidor10.com.br/fiis/" + ticker;
        logger.info("Iniciando scraping com Selenium para a url {}", urlCompleta);

        // 1. Isole a criação do recurso bloqueante (o WebDriver) em seu próprio Mono
        Mono<ChromeDriver> driverMono = Mono.fromCallable(() -> {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");
            return new ChromeDriver(options);
        });

        // 2. Use flatMap para encadear o restante das operações, garantindo que elas só sejam executadas se o driver for criado
        return driverMono.flatMap(driver -> {
                    DevTools devTools = driver.getDevTools();
                    devTools.createSession();

                    // A lógica principal de scraping está encapsulada em um novo Mono.fromCallable
                    return Mono.fromCallable(() -> {
                                // Configurar o listener de rede
                                devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
                                final Map<String, String> urlsMapeadas = new ConcurrentHashMap<>();
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

                                // Executar a interação com a página
                                driver.get(urlCompleta);
                                Thread.sleep(6000); // Aguardar a captura das chamadas iniciais da API
                                String html = driver.getPageSource();

                                // Empacotar os resultados para a próxima etapa
                                return new ScrapeResult(html, urlsMapeadas);
                            })
                            // 3. doFinally garante que o driver será fechado, independentemente do que acontecer
                            .doFinally(signalType -> {
                                logger.info("Finalizando a sessão do Selenium para {}. Sinal: {}", ticker, signalType);
                                devTools.close();
                                driver.quit();
                            });
                })
                // 4. Este flatMap é a segunda etapa reativa do nosso pipeline

                .flatMap(result -> {
                    Document doc = Jsoup.parse(result.html());

                    // Processar os dados síncronos do HTML
                    FiiInfoHeaderDTO infoHeader = fiiHeaderScraper.scrape(doc);
                    Integer internalId = fiiInternalIdScrapper.scrape(new ArrayList<>(result.urlsMapeadas().values()));
                    // TODO: Call your other HTML scrapers here (FiiInfoSobreScraper, etc.)

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
                                        new FiiInfoSobreDTO(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null), // Placeholder
                                        new FiiInfoCardsDTO(null, null), // Placeholder
                                        dividendos,
                                        cotacao
                                );
                            });
                })
                // Esses operadores se aplicam a toda a cadeia reativa
                .doOnError(error -> logger.error("O Mono do FiiSeleniumScraperAdapter falhou para o ticker {}", ticker, error))
                .subscribeOn(Schedulers.boundedElastic()); // Ensures the entire operation runs on a dedicated thread
    }
}
