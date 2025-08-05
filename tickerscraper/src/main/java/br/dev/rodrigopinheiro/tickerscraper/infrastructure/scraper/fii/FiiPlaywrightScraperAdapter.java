package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.*;

import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.FiiApiConstants.*;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Component("fiiPlaywrightScraper")
public class FiiPlaywrightScraperAdapter implements FiiDataScrapperPort {

    private static final Logger logger = LoggerFactory.getLogger(FiiPlaywrightScraperAdapter.class);

    private final FiiHeaderScraper fiiHeaderScraper;
    private final FiiApiScraper fiiApiScraper;
    private final FiiInternalIdScrapper fiiInternalIdScrapper;
    private final FiiInfoSobreScraper fiiInfoSobreScraper;
    private final FiiCardsScraper fiiCardsScraper;

    // Helper record to pass results from the Playwright block to the reactive chain
    private record ScrapeResult(String html, Map<String, String> urlsMapeadas) {
        public List<String> getUrlList() {
            return new ArrayList<>(urlsMapeadas.values());
        }
    }


    public FiiPlaywrightScraperAdapter(FiiHeaderScraper fiiHeaderScraper, FiiApiScraper fiiApiScraper, FiiInternalIdScrapper fiiInternalIdScrapper, FiiInfoSobreScraper fiiInfoSobreScraper, FiiCardsScraper fiiCardsScraper) {
        this.fiiHeaderScraper = fiiHeaderScraper;
        this.fiiApiScraper = fiiApiScraper;
        this.fiiInternalIdScrapper = fiiInternalIdScrapper;
        this.fiiInfoSobreScraper = fiiInfoSobreScraper;
        this.fiiCardsScraper = fiiCardsScraper;
    }

    @Override
    public Mono<FiiDadosFinanceirosDTO> scrape(String ticker) {
        String urlCompleta = "https://investidor10.com.br/fiis/" + ticker;
        logger.info("Iniciando scraping com Playwright para a url {}", urlCompleta);

        // 1. Wrap the blocking Playwright operation in Mono.fromCallable.
        // This allows integrating Playwright's synchronous API into a reactive chain.
        return Mono.fromCallable(() -> {
                    // 2. Use try-with-resources for automatic resource management.
                    // This is the idiomatic way to handle Playwright resources and replaces doFinally/quit().
                    try (Playwright playwright = Playwright.create()) {
                        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                                .setHeadless(true)
                                .setArgs(List.of("--window-size=1920,1080", "--disable-blink-features=AutomationControlled"));

                        Browser browser = playwright.chromium().launch(launchOptions);
                        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                        );
                        Page page = context.newPage();

                        final Map<String, String> urlsMapeadas = new ConcurrentHashMap<>();

                        // 3. Set up network interception using page.onRequest().
                        // This is Playwright's elegant equivalent to Selenium's DevTools listener.
                        page.onRequest(request -> {
                            String url = request.url();
                            for (String chave : FiiApiConstants.TODAS_AS_CHAVES) {
                                if (url.contains(chave)) {
                                    urlsMapeadas.putIfAbsent(chave, url);
                                    logger.info(">> URL de API do tipo '{}' CAPTURADA: {}", chave, url);
                                    break; // Stop after first match for this request
                                }
                            }
                        });

                        // 4. Navigate and wait intelligently.
                        // WaitUntilState.NETWORKIDLE is more reliable than a fixed Thread.sleep().
                        // It waits until there have been no network connections for 500ms.
                        logger.info("Navegando e aguardando a rede ficar ociosa...");
                        page.navigate(urlCompleta, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

                        String html = page.content();
                        logger.info("Conteúdo HTML e chamadas de API capturadas para {}", ticker);

                        // Browser and context are closed automatically by try-with-resources
                        return new ScrapeResult(html, urlsMapeadas);
                    }
                })
                // 5. flatMap to process the result and chain subsequent async operations.
                // This structure is identical to the original Selenium version.
                .flatMap(result -> {
                    Document doc = Jsoup.parse(result.html());

                    // The rest of the logic is UNCHANGED, as it operates on the collected data.
                    FiiInfoHeaderDTO infoHeader = fiiHeaderScraper.scrape(doc);
                    Integer internalId = fiiInternalIdScrapper.scrape(result.getUrlList());
                    FiiInfoSobreDTO infoSobreDTO = fiiInfoSobreScraper.scrape(doc);
                    FiiInfoCardsDTO infoCardsDTO = fiiCardsScraper.scrape(doc);

                    // Disparar as chamadas assíncronas da API (reusing existing Monos)
                    Mono<FiiCotacaoDTO> cotacaoMono = result.urlsMapeadas().containsKey(COTACAO)
                            ? fiiApiScraper.fetchCotacao(result.urlsMapeadas().get(COTACAO))
                            : Mono.just(new FiiCotacaoDTO(null, null));

                    Mono<List<FiiDividendoDTO>> dividendosMono = result.urlsMapeadas().containsKey(DIVIDENDOS)
                            ? fiiApiScraper.fetchDividendos(result.urlsMapeadas().get(DIVIDENDOS))
                            : Mono.just(Collections.emptyList());

                    Mono<FiiIndicadorHistoricoDTO> historicoMono = result.urlsMapeadas().containsKey(HISTORICO_INDICADORES)
                            ? fiiApiScraper.fetchHistorico(result.urlsMapeadas().get(HISTORICO_INDICADORES))
                            : Mono.just(new FiiIndicadorHistoricoDTO(Collections.emptyMap()));

                    // 6. Use Mono.zip to combine results, just like before.
                    return Mono.zip(cotacaoMono, dividendosMono, historicoMono)
                            .map(tuple -> new FiiDadosFinanceirosDTO(
                                    internalId,
                                    infoHeader,
                                    tuple.getT3(),
                                    infoSobreDTO,
                                    infoCardsDTO,
                                    tuple.getT2(),
                                    tuple.getT1()
                            ));
                })
                // These operators apply to the entire reactive chain
                .doOnError(error -> logger.error("O Mono do FiiPlaywrightScraperAdapter falhou para o ticker {}", ticker, error))
                .subscribeOn(Schedulers.boundedElastic()); // Ensures the blocking Playwright part runs on a suitable thread.
    }
}