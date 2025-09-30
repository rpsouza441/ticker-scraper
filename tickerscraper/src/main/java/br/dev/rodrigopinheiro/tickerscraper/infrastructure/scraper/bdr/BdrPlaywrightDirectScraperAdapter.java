package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.BdrDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.AntiBotDetectedException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingTimeoutException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.TickerNotFoundException;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.PlaywrightInitializer;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base.AbstractScraperAdapter;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.CapturedRequest;
import com.microsoft.playwright.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrApiConstants.*;

@Component
@Qualifier("BdrPlaywrightDirectScraperAdapter")
public class BdrPlaywrightDirectScraperAdapter extends AbstractScraperAdapter<BdrDadosFinanceirosDTO> implements BdrDataScrapperPort {

    private static final Logger log = LoggerFactory.getLogger(BdrPlaywrightDirectScraperAdapter.class);

    private final PlaywrightInitializer playwrightInitializer;
    private final BdrApiScraper apiScraper;
    private final BdrCardsScraper cardsScraper;
    private final BdrHeaderScraper headerScraper;
    private final BdrInfoSobreScraper sobreScraper;

    public BdrPlaywrightDirectScraperAdapter(
            PlaywrightInitializer playwrightInitializer,
            BdrHeaderScraper headerScraper,
            BdrCardsScraper cardsScraper,
            BdrInfoSobreScraper sobreScraper,
            BdrApiScraper apiScraper) {
        this.playwrightInitializer = playwrightInitializer;
        this.headerScraper = headerScraper;
        this.cardsScraper = cardsScraper;
        this.sobreScraper = sobreScraper;
        this.apiScraper = apiScraper;
    }

    @Override
    @Retry(name = "bdrScraper")
    @CircuitBreaker(name = "bdrScraper")
    public Mono<BdrDadosFinanceirosDTO> scrape(String ticker) {
        final String url = buildUrl(ticker);
        AtomicReference<BrowserContext> ctxRef = new AtomicReference<>();
        AtomicReference<Page> pageRef = new AtomicReference<>();

        return createReactiveStructureForMono(() -> {
            log.info("Iniciando scraping de BDR para: {}", ticker);
            BrowserContext ctx = createPlaywrightContext(playwrightInitializer.getBrowser());
            Page page = createPlaywrightPage(ctx);
            ctxRef.set(ctx);
            pageRef.set(page);

            try {
                final Map<String, CapturedRequest> requests = new ConcurrentHashMap<>();
                page.onRequest(req -> {
                    String u = req.url();
                    for (String key : TODAS_AS_CHAVES) {
                        if (!requests.containsKey(key) && u.contains(key)) {
                            requests.put(key, new CapturedRequest(u, req.headers()));
                            log.info("API de BDR capturada ({}): {}", key, u);
                        }
                    }
                });

                navigateAndValidate(page, url, ticker);

                String html = page.content();
                Document doc = Jsoup.parse(html);
                validateEssentialElements(doc, getEssentialSelectors(), getCardsSelectors(), ticker, url);

                InfoHeader header = headerScraper.extract(doc, ticker);
                InfoCards cards = cardsScraper.extract(doc);
                InfoSobre sobre = sobreScraper.extract(doc);

                Mono<Map<String, Object>> indicadoresMono = Optional.ofNullable(requests.get(HIST_INDICADORES))
                        .map(req -> apiScraper.fetchIndicadores(req.url(), req.headers()))
                        .orElse(Mono.just(Collections.emptyMap()));
                Mono<Map<String, Object>> dreMono = Optional.ofNullable(requests.get(DRE))
                        .map(req -> apiScraper.fetchDre(req.url(), req.headers()))
                        .orElse(Mono.just(Collections.emptyMap()));
                Mono<Map<String, Object>> bpMono = Optional.ofNullable(requests.get(BALANCO_PATRIMONIAL))
                        .map(req -> apiScraper.fetchBalancoPatrimonial(req.url(), req.headers()))
                        .orElse(Mono.just(Collections.emptyMap()));
                Mono<Map<String, Object>> fcMono = Optional.ofNullable(requests.get(FLUXO_CAIXA))
                        .map(req -> apiScraper.fetchFluxoCaixa(req.url(), req.headers()))
                        .orElse(Mono.just(Collections.emptyMap()));
                Mono<Map<String, Object>> dividendosMono = Optional.ofNullable(requests.get(DIVIDENDOS))
                        .map(req -> apiScraper.fetchDividendos(req.url(), req.headers()))
                        .orElse(Mono.just(Collections.emptyMap()));

                return Mono.zip(indicadoresMono, dreMono, bpMono, fcMono, dividendosMono)
                        .map(tuple -> new BdrDadosFinanceirosDTO(
                                header, cards, sobre,
                                tuple.getT1(), // indicadores
                                new Demonstrativos(tuple.getT2(), tuple.getT3(), tuple.getT4()),
                                tuple.getT5(), // dividendos
                                Instant.now()
                        ));

                // --- TRADUÇÃO DE ERROS TÉCNICOS PARA EXCEÇÕES DE DOMÍNIO ---
            } catch (TimeoutError e) {
                throw new ScrapingTimeoutException(ticker, url, Duration.ofMillis(DEFAULT_TIMEOUT_MS), "PLAYWRIGHT_OPERATION");
            } catch (PlaywrightException e) {
                if (e.getMessage().contains("net::ERR_NAME_NOT_RESOLVED") || e.getMessage().contains("404")) {
                    throw new TickerNotFoundException(ticker, url);
                }
                throw new AntiBotDetectedException(ticker, url, "Erro inesperado do Playwright: " + e.getMessage(), "Playwright");
            } catch (Exception e) {
                // Captura qualquer outra exceção e a envolve em uma exceção de scraping
                throw new ScrapingException("Erro não esperado durante o scraping de BDR", ticker, url, "UNKNOWN", e) {
                    @Override
                    public String getErrorCode() {
                        return "BDR_SCRAPE_FAILED";
                    }
                };
            }
            // --- FIM DO BLOCO TRY-CATCH ---

        }, ticker, () -> closePlaywrightResources(pageRef.get(), ctxRef.get()));
    }

    @Override
    protected String[] getEssentialSelectors() {
        return new String[]{"#table-indicators-company"};
    }

    @Override
    protected String[] getCardsSelectors() {
        return new String[]{"div._card"};
    }

    @Override
    protected String buildUrl(String ticker) {
        return String.format("https://investidor10.com.br/bdrs/%s/", ticker);
    }

    @Override
    protected BdrDadosFinanceirosDTO executeSpecificScraping(Document doc, String ticker) {
        InfoHeader header = headerScraper.extract(doc, ticker);
        InfoCards cards = cardsScraper.extract(doc);
        InfoSobre sobre = sobreScraper.extract(doc);
        return new BdrDadosFinanceirosDTO(
                header,
                cards,
                sobre,
                java.util.Collections.emptyMap(), // indicadores
                new Demonstrativos(
                        java.util.Collections.emptyMap(), // dre
                        java.util.Collections.emptyMap(), // bp
                        java.util.Collections.emptyMap()  // fc
                ),
                java.util.Collections.emptyMap(),     // dividendos
                java.time.Instant.now()
        );    }
}