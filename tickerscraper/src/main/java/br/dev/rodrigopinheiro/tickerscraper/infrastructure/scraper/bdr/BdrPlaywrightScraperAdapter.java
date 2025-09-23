package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.BdrDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.AsyncRequestTimeoutException;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.PlaywrightInitializer;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base.AbstractScraperAdapter;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.network.BdrNetworkPayloadCollector;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.network.BdrNetworkPayloadDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.parser.BdrCotacoesParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.parser.BdrDemonstrativosParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.parser.BdrDividendosParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.parser.BdrIndicadoresParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.common.CorrelationIdProvider;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrApiConstants.*;

/**
 * Adapter Playwright responsável por capturar o tráfego XHR da página de BDRs e consolidar
 * as respostas das APIs em um DTO unificado.
 */
@Component("bdrPlaywrightScraper")
public class BdrPlaywrightScraperAdapter extends AbstractScraperAdapter<BdrDadosFinanceirosDTO>
        implements BdrDataScrapperPort {

    private static final Logger logger = LoggerFactory.getLogger(BdrPlaywrightScraperAdapter.class);

    private static final String[] ESSENTIAL_SELECTORS = {"main", "div.container", "section.bdr-header"};
    private static final String[] CARDS_SELECTORS = {"section#cards-ticker", "section.cards", "div.cards"};

    private final PlaywrightInitializer playwrightInitializer;
    private final CorrelationIdProvider correlationIdProvider;
    private final BdrNetworkPayloadCollector networkPayloadCollector;
    private final BdrCotacoesParser cotacoesParser;
    private final BdrDividendosParser dividendosParser;
    private final BdrIndicadoresParser indicadoresParser;
    private final BdrDemonstrativosParser demonstrativosParser;

    public BdrPlaywrightScraperAdapter(PlaywrightInitializer playwrightInitializer,
                                       CorrelationIdProvider correlationIdProvider,
                                       BdrNetworkPayloadCollector networkPayloadCollector,
                                       BdrCotacoesParser cotacoesParser,
                                       BdrDividendosParser dividendosParser,
                                       BdrIndicadoresParser indicadoresParser,
                                       BdrDemonstrativosParser demonstrativosParser) {
        this.playwrightInitializer = playwrightInitializer;
        this.correlationIdProvider = correlationIdProvider;
        this.networkPayloadCollector = networkPayloadCollector;
        this.cotacoesParser = cotacoesParser;
        this.dividendosParser = dividendosParser;
        this.indicadoresParser = indicadoresParser;
        this.demonstrativosParser = demonstrativosParser;
    }

    @Override
    @CircuitBreaker(name = "scraper", fallbackMethod = "fallbackToHtmlOnly")
    @Retry(name = "scraper")
    public Mono<BdrDadosFinanceirosDTO> scrape(String ticker) {
        final String normalizedTicker = ticker == null ? null : ticker.trim().toUpperCase();
        if (normalizedTicker == null || normalizedTicker.isBlank()) {
            return Mono.error(new IllegalArgumentException("Ticker inválido para scraping de BDR"));
        }

        final String url = buildUrl(normalizedTicker);
        final AtomicReference<BrowserContext> ctxRef = new AtomicReference<>();
        final AtomicReference<Page> pageRef = new AtomicReference<>();

        return createReactiveStructure(() -> {
            Browser browser = playwrightInitializer.getBrowser();
            BrowserContext context = createPlaywrightContext(browser);
            Page page = createPlaywrightPage(context);
            ctxRef.set(context);
            pageRef.set(page);

            BdrNetworkPayloadCollector.CaptureSession captureSession = networkPayloadCollector.start(page);

            String correlationId = correlationIdProvider.getCurrentCorrelationIdOrDefault("unknown");

            Response response = navigateAndValidate(page, url, normalizedTicker);
            if (response == null) {
                logger.warn("Resposta nula ao navegar para {}", url);
            }

            page.waitForLoadState(LoadState.NETWORKIDLE);

            BdrNetworkPayloadDTO networkPayload = networkPayloadCollector.awaitAndCollect(
                    captureSession,
                    normalizedTicker,
                    correlationId
            );

            String html = page.content();
            Document document = Jsoup.parse(html);
            validateEssentialElements(document, getEssentialSelectors(), getCardsSelectors(), normalizedTicker, url);

            BdrHtmlMetadataDTO metadata = extractMetadata(document, html);
            BdrIndicadoresDTO indicadores = indicadoresParser.parse(networkPayload.payloads().get(KEY_INDICADORES));

            // Se não houver paridade nas APIs, tenta extrair do HTML
            if (indicadores.paridade() == null) {
                IndicadorParser.ParidadeBdrInfo paridadeFromHtml =
                        extractParidadeFromHtml(document).orElse(null);
                indicadores = new BdrIndicadoresDTO(
                        indicadores.indicadoresMonetarios(),
                        indicadores.indicadoresPercentuais(),
                        indicadores.indicadoresSimples(),
                        paridadeFromHtml,
                        indicadores.moedaPadrao(),
                        indicadores.raw()
                );
            }

            BdrCotacoesDTO cotacoes = cotacoesParser.parse(networkPayload.payloads().get(KEY_COTACOES));
            BdrDividendosDTO dividendos = dividendosParser.parse(networkPayload.payloads().get(KEY_DIVIDENDOS));
            BdrDemonstrativoDTO dre = demonstrativosParser.parse(networkPayload.payloads().get(KEY_DRE), "DRE");
            BdrDemonstrativoDTO bp = demonstrativosParser.parse(networkPayload.payloads().get(KEY_BP), "BP");
            BdrDemonstrativoDTO fc = demonstrativosParser.parse(networkPayload.payloads().get(KEY_FC), "FC");

            Map<String, String> rawJson = buildRawJson(networkPayload.payloads());

            Instant updatedAt = Instant.now(Clock.systemUTC());

            return new BdrDadosFinanceirosDTO(
                    normalizedTicker,
                    networkPayload.investidorId(),
                    cotacoes,
                    dividendos,
                    indicadores,
                    dre,
                    bp,
                    fc,
                    metadata,
                    rawJson,
                    updatedAt
            );
        }, normalizedTicker, () -> closePlaywrightResources(pageRef.get(), ctxRef.get()))
                .timeout(java.time.Duration.ofMillis(ASYNC_OPERATION_TIMEOUT_MS))
                .onErrorMap(java.util.concurrent.TimeoutException.class,
                        ex -> AsyncRequestTimeoutException.forPlaywrightScraping(
                                normalizedTicker,
                                java.time.Duration.ofMillis(ASYNC_OPERATION_TIMEOUT_MS),
                                correlationIdProvider.getCurrentCorrelationIdOrDefault("unknown")));
    }

    /**
     * Fallback utilizado pelo circuito quando ocorre uma exceção inesperada.
     * Retorna apenas os metadados HTML sem dados de APIs, preservando rawJson vazio.
     */
    public Mono<BdrDadosFinanceirosDTO> fallbackToHtmlOnly(String ticker, Exception cause) {
        logger.warn("Fallback HTML-only acionado para {} devido a {}", ticker, cause.toString());
        final String normalizedTicker = ticker == null ? null : ticker.trim().toUpperCase();
        if (normalizedTicker == null) {
            return Mono.error(cause);
        }
        return createReactiveStructure(() -> {
            Browser browser = playwrightInitializer.getBrowser();
            BrowserContext context = createPlaywrightContext(browser);
            Page page = createPlaywrightPage(context);
            try {
                navigateAndValidate(page, buildUrl(normalizedTicker), normalizedTicker);
                String html = page.content();
                Document doc = Jsoup.parse(html);
                BdrHtmlMetadataDTO metadata = extractMetadata(doc, html);
                Instant updatedAt = Instant.now(Clock.systemUTC());
                return new BdrDadosFinanceirosDTO(
                        normalizedTicker,
                        null,
                        BdrCotacoesDTO.empty(),
                        BdrDividendosDTO.empty(),
                        BdrIndicadoresDTO.empty(),
                        BdrDemonstrativoDTO.empty("DRE"),
                        BdrDemonstrativoDTO.empty("BP"),
                        BdrDemonstrativoDTO.empty("FC"),
                        metadata,
                        Map.of(),
                        updatedAt
                );
            } finally {
                closePlaywrightResources(page, context);
            }
        }, normalizedTicker, () -> {});
    }

    private BdrHtmlMetadataDTO extractMetadata(Document document, String html) {
        String title = document.title();
        String description = Optional.ofNullable(document.selectFirst("meta[name=description]")).map(e -> e.attr("content")).orElse(null);
        Map<String, String> metaTags = new LinkedHashMap<>();
        for (Element meta : document.select("meta[name], meta[property]")) {
            String name = Optional.ofNullable(meta.attr("name")).filter(v -> !v.isBlank()).orElse(meta.attr("property"));
            if (name != null && !name.isBlank()) {
                metaTags.put(name, meta.attr("content"));
            }
        }
        return new BdrHtmlMetadataDTO(title, description, metaTags, html);
    }

    private Optional<IndicadorParser.ParidadeBdrInfo> extractParidadeFromHtml(Document document) {
        if (document == null) {
            return Optional.empty();
        }
        String text = document.text();
        Pattern pattern = Pattern.compile("(?i)paridade[^:]*:([^\\n]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String raw = matcher.group(1).trim();
            return IndicadorParser.parseParidadeBdr(raw);
        }
        return Optional.empty();
    }

    private Map<String, String> buildRawJson(Map<String, String> payloads) {
        if (payloads.isEmpty()) {
            return Map.of();
        }
        Map<String, String> ordered = new LinkedHashMap<>();
        for (String key : ALL_KEYS) {
            if (payloads.containsKey(key)) {
                ordered.put(key, payloads.get(key));
            }
        }
        payloads.forEach((k, v) -> ordered.putIfAbsent(k, v));
        return Collections.unmodifiableMap(ordered);
    }

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
        return BASE_URL + ticker;
    }

    @Override
    protected BdrDadosFinanceirosDTO executeSpecificScraping(Document doc, String ticker) {
        logger.debug("Executando scraping básico de HTML para {}", ticker);
        String html = doc.html();
        BdrHtmlMetadataDTO metadata = extractMetadata(doc, html);
        Instant updatedAt = Instant.now(Clock.systemUTC());
        return new BdrDadosFinanceirosDTO(
                ticker,
                null,
                BdrCotacoesDTO.empty(),
                BdrDividendosDTO.empty(),
                BdrIndicadoresDTO.empty(),
                BdrDemonstrativoDTO.empty("DRE"),
                BdrDemonstrativoDTO.empty("BP"),
                BdrDemonstrativoDTO.empty("FC"),
                metadata,
                Map.of(),
                updatedAt
        );
    }
}
