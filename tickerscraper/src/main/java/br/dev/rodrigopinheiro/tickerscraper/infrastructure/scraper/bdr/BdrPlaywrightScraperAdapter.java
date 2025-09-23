package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.BdrDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.AsyncRequestTimeoutException;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.PlaywrightInitializer;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base.AbstractScraperAdapter;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.common.CorrelationIdProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern INVESTIDOR_ID_PATTERN = Pattern.compile("/chart/(\\d+)/");
    private static final Pattern BALANCO_TIPO_PATTERN = Pattern.compile("/balancos/(\\d+)/(DRE|BP|FC)/", Pattern.CASE_INSENSITIVE);

    private final PlaywrightInitializer playwrightInitializer;
    private final CorrelationIdProvider correlationIdProvider;
    private final ObjectMapper objectMapper;

    public BdrPlaywrightScraperAdapter(PlaywrightInitializer playwrightInitializer,
                                       CorrelationIdProvider correlationIdProvider,
                                       ObjectMapper objectMapper) {
        this.playwrightInitializer = playwrightInitializer;
        this.correlationIdProvider = correlationIdProvider;
        this.objectMapper = objectMapper;
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

            Map<String, String> payloads = new ConcurrentHashMap<>();
            AtomicReference<String> investidorId = new AtomicReference<>();

            page.onResponse(response -> handleResponse(response, payloads, investidorId));

            String correlationId = correlationIdProvider.getCurrentCorrelationIdOrDefault("unknown");

            Response response = navigateAndValidate(page, url, normalizedTicker);
            if (response == null) {
                logger.warn("Resposta nula ao navegar para {}", url);
            }

            page.waitForLoadState(LoadState.NETWORKIDLE);

            waitForPayload(payloads, KEY_COTACOES, normalizedTicker, correlationId);
            waitForPayload(payloads, KEY_DIVIDENDOS, normalizedTicker, correlationId);
            waitForPayload(payloads, KEY_INDICADORES, normalizedTicker, correlationId);
            waitForPayload(payloads, KEY_DRE, normalizedTicker, correlationId);
            waitForPayload(payloads, KEY_BP, normalizedTicker, correlationId);
            waitForPayload(payloads, KEY_FC, normalizedTicker, correlationId);

            String html = page.content();
            Document document = Jsoup.parse(html);
            validateEssentialElements(document, getEssentialSelectors(), getCardsSelectors(), normalizedTicker, url);

            BdrHtmlMetadataDTO metadata = extractMetadata(document, html);
            BdrIndicadoresDTO indicadores = parseIndicadores(payloads.get(KEY_INDICADORES));

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

            BdrCotacoesDTO cotacoes = parseCotacoes(payloads.get(KEY_COTACOES));
            BdrDividendosDTO dividendos = parseDividendos(payloads.get(KEY_DIVIDENDOS));
            BdrDemonstrativoDTO dre = parseDemonstrativo(payloads.get(KEY_DRE), "DRE");
            BdrDemonstrativoDTO bp = parseDemonstrativo(payloads.get(KEY_BP), "BP");
            BdrDemonstrativoDTO fc = parseDemonstrativo(payloads.get(KEY_FC), "FC");

            Map<String, String> rawJson = buildRawJson(payloads);

            Instant updatedAt = Instant.now(Clock.systemUTC());

            return new BdrDadosFinanceirosDTO(
                    normalizedTicker,
                    investidorId.get(),
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

    private void handleResponse(Response response,
                                 Map<String, String> payloads,
                                 AtomicReference<String> investidorId) {
        String url = response.url();
        try {
            if (url.contains(COTACOES_CHART_PATH) && payloads.putIfAbsent(KEY_COTACOES, response.text()) == null) {
                extractInvestidorId(url).ifPresent(id -> investidorId.compareAndSet(null, id));
                logger.debug("Capturada API de cotações: {}", url);
            } else if (url.contains(DIVIDENDOS_PATH) && payloads.putIfAbsent(KEY_DIVIDENDOS, response.text()) == null) {
                extractInvestidorId(url).ifPresent(id -> investidorId.compareAndSet(null, id));
                logger.debug("Capturada API de dividendos: {}", url);
            } else if (url.contains(HISTORICO_INDICADORES_PATH) &&
                    payloads.putIfAbsent(KEY_INDICADORES, response.text()) == null) {
                extractInvestidorId(url).ifPresent(id -> investidorId.compareAndSet(null, id));
                logger.debug("Capturada API de indicadores: {}", url);
            } else if (url.contains(BALANCO_DRE_PATH)) {
                Matcher matcher = BALANCO_TIPO_PATTERN.matcher(url);
                if (matcher.find()) {
                    String id = matcher.group(1);
                    String tipo = matcher.group(2).toUpperCase();
                    extractInvestidorId(id).ifPresent(value -> investidorId.compareAndSet(null, value));
                    switch (tipo) {
                        case "DRE" -> {
                            if (payloads.putIfAbsent(KEY_DRE, response.text()) == null) {
                                logger.debug("Capturada API de DRE: {}", url);
                            }
                        }
                        case "BP" -> {
                            if (payloads.putIfAbsent(KEY_BP, response.text()) == null) {
                                logger.debug("Capturada API de BP: {}", url);
                            }
                        }
                        case "FC" -> {
                            if (payloads.putIfAbsent(KEY_FC, response.text()) == null) {
                                logger.debug("Capturada API de FC: {}", url);
                            }
                        }
                        default -> {
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Falha ao processar resposta {}: {}", url, ex.getMessage());
        }
    }

    private Optional<String> extractInvestidorId(String value) {
        if (value == null) {
            return Optional.empty();
        }
        Matcher matcher = INVESTIDOR_ID_PATTERN.matcher(value);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(1));
        }
        if (value.matches("\\d+")) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    private void waitForPayload(Map<String, String> payloads, String key, String ticker, String correlationId) {
        int waited = 0;
        int step = 200;
        while (!payloads.containsKey(key) && waited < NETWORK_CAPTURE_TIMEOUT_MS) {
            try {
                Thread.sleep(step);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            waited += step;
        }
        if (!payloads.containsKey(key)) {
            logger.warn("Timeout aguardando payload '{}' para {} (correlationId={})", key, ticker, correlationId);
        }
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

    private BdrCotacoesDTO parseCotacoes(String json) {
        if (json == null || json.isBlank()) {
            return BdrCotacoesDTO.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode dataNode = root.has("data") ? root.get("data") : root;
            if (!dataNode.isArray()) {
                dataNode = root;
            }
            List<BdrPricePointDTO> pontos = new ArrayList<>();
            for (JsonNode node : dataNode) {
                Instant timestamp = parseInstant(node);
                BigDecimal valor = parseValor(node);
                if (valor != null) {
                    pontos.add(new BdrPricePointDTO(timestamp, valor));
                }
            }
            if (pontos.size() > 365) {
                pontos = pontos.subList(Math.max(0, pontos.size() - 365), pontos.size());
            }
            String moeda = Optional.ofNullable(root.get("currency"))
                    .filter(JsonNode::isTextual)
                    .map(JsonNode::asText)
                    .orElseGet(() -> IndicadorParser.extrairMoeda(json).orElse(null));
            return new BdrCotacoesDTO(List.copyOf(pontos), moeda, root);
        } catch (Exception ex) {
            logger.warn("Erro ao parsear cotações de BDR: {}", ex.getMessage());
            return BdrCotacoesDTO.empty();
        }
    }

    private Instant parseInstant(JsonNode node) {
        if (node == null) {
            return null;
        }
        JsonNode valueNode = node.isArray() && node.size() > 0 ? node.get(0) : node.get("date");
        if (valueNode == null) {
            valueNode = node.get("x");
        }
        if (valueNode == null) {
            valueNode = node.get("timestamp");
        }
        if (valueNode == null) {
            return null;
        }
        if (valueNode.isNumber()) {
            long epoch = valueNode.asLong();
            if (String.valueOf(epoch).length() == 13) {
                return Instant.ofEpochMilli(epoch);
            }
            return Instant.ofEpochSecond(epoch);
        }
        if (valueNode.isTextual()) {
            String text = valueNode.asText();
            try {
                return Instant.parse(text);
            } catch (DateTimeParseException ex) {
                try {
                    LocalDate date = LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
                    return date.atStartOfDay(ZoneOffset.UTC).toInstant();
                } catch (Exception ignored) {
                    try {
                        LocalDateTime dateTime = LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        return dateTime.atZone(ZoneOffset.UTC).toInstant();
                    } catch (Exception ignoredAgain) {
                        logger.debug("Formato de data não suportado para valor '{}': {}", text, ignoredAgain.getMessage());
                    }
                }
            }
        }
        return null;
    }

    private BigDecimal parseValor(JsonNode node) {
        JsonNode valueNode = node.isArray() && node.size() > 1 ? node.get(1) : node.get("value");
        if (valueNode == null) {
            valueNode = node.get("y");
        }
        if (valueNode == null) {
            return null;
        }
        if (valueNode.isNumber()) {
            return valueNode.decimalValue();
        }
        if (valueNode.isTextual()) {
            return IndicadorParser.parseValorMonetario(valueNode.asText()).orElse(null);
        }
        return null;
    }

    private BdrDividendosDTO parseDividendos(String json) {
        if (json == null || json.isBlank()) {
            return BdrDividendosDTO.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode dataNode = root.has("data") ? root.get("data") : root;
            if (!dataNode.isArray()) {
                dataNode = root;
            }
            List<BdrDividendoItemDTO> itens = new ArrayList<>();
            for (JsonNode item : dataNode) {
                String periodo = Optional.ofNullable(item.get("periodo"))
                        .filter(JsonNode::isTextual)
                        .map(JsonNode::asText)
                        .orElseGet(() -> Optional.ofNullable(item.get("label"))
                                .filter(JsonNode::isTextual)
                                .map(JsonNode::asText)
                                .orElse(null));
                BigDecimal valor = null;
                if (item.has("valor")) {
                    valor = IndicadorParser.parseValorMonetario(item.get("valor").asText()).orElse(null);
                } else if (item.has("value")) {
                    JsonNode valueNode = item.get("value");
                    if (valueNode.isNumber()) {
                        valor = valueNode.decimalValue();
                    } else if (valueNode.isTextual()) {
                        valor = IndicadorParser.parseValorMonetario(valueNode.asText()).orElse(null);
                    }
                }
                String moeda = null;
                if (item.has("valor")) {
                    moeda = IndicadorParser.extrairMoeda(item.get("valor").asText()).orElse(null);
                }
                if (valor != null) {
                    itens.add(new BdrDividendoItemDTO(periodo, valor, moeda));
                }
            }
            if (itens.size() > 5) {
                itens = itens.subList(Math.max(0, itens.size() - 5), itens.size());
            }
            return new BdrDividendosDTO(List.copyOf(itens), root);
        } catch (Exception ex) {
            logger.warn("Erro ao parsear dividendos de BDR: {}", ex.getMessage());
            return BdrDividendosDTO.empty();
        }
    }

    private BdrIndicadoresDTO parseIndicadores(String json) {
        if (json == null || json.isBlank()) {
            return BdrIndicadoresDTO.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            Map<String, BigDecimal> monetarios = new LinkedHashMap<>();
            Map<String, BigDecimal> percentuais = new LinkedHashMap<>();
            Map<String, Double> simples = new LinkedHashMap<>();
            String moedaPadrao = null;
            IndicadorParser.ParidadeBdrInfo paridade = null;

            if (root.isArray()) {
                for (JsonNode node : root) {
                    processIndicatorNode(node, monetarios, percentuais, simples);
                    if (moedaPadrao == null && node.has("valor")) {
                        moedaPadrao = IndicadorParser.extrairMoeda(node.get("valor").asText()).orElse(null);
                    }
                    if (paridade == null && node.has("valor")) {
                        paridade = IndicadorParser.parseParidadeBdr(node.get("valor").asText()).orElse(null);
                    }
                }
            } else if (root.isObject()) {
                root.fields().forEachRemaining(entry -> {
                    JsonNode value = entry.getValue();
                    if (value.isTextual()) {
                        String texto = value.asText();
                        IndicadorParser.parseValorMonetario(texto)
                                .ifPresent(valor -> monetarios.put(entry.getKey(), valor));
                        IndicadorParser.parsePercentualParaDecimal(texto)
                                .ifPresent(p -> percentuais.put(entry.getKey(), p));
                        IndicadorParser.safeParseDouble(texto)
                                .ifPresent(v -> simples.put(entry.getKey(), v));
                        if (moedaPadrao == null) {
                            moedaPadrao = IndicadorParser.extrairMoeda(texto).orElse(null);
                        }
                        if (paridade == null) {
                            paridade = IndicadorParser.parseParidadeBdr(texto).orElse(null);
                        }
                    } else if (value.isNumber()) {
                        simples.put(entry.getKey(), value.doubleValue());
                    }
                });
            }

            return new BdrIndicadoresDTO(monetarios, percentuais, simples, paridade, moedaPadrao, root);
        } catch (Exception ex) {
            logger.warn("Erro ao parsear indicadores de BDR: {}", ex.getMessage());
            return BdrIndicadoresDTO.empty();
        }
    }

    private void processIndicatorNode(JsonNode node,
                                      Map<String, BigDecimal> monetarios,
                                      Map<String, BigDecimal> percentuais,
                                      Map<String, Double> simples) {
        String nome = Optional.ofNullable(node.get("nome"))
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .orElse(null);
        JsonNode valorNode = node.get("valor");
        if (valorNode == null && node.has("value")) {
            valorNode = node.get("value");
        }
        if (valorNode == null) {
            return;
        }
        if (valorNode.isNumber()) {
            simples.put(nome, valorNode.doubleValue());
            return;
        }
        if (valorNode.isTextual()) {
            String texto = valorNode.asText();
            IndicadorParser.parseValorMonetario(texto).ifPresent(valor -> {
                if (nome != null) {
                    monetarios.put(nome, valor);
                }
            });
            IndicadorParser.parsePercentualParaDecimal(texto).ifPresent(valor -> {
                if (nome != null) {
                    percentuais.put(nome, valor);
                }
            });
            IndicadorParser.safeParseDouble(texto).ifPresent(valor -> {
                if (nome != null) {
                    simples.put(nome, valor);
                }
            });
        }
    }

    private BdrDemonstrativoDTO parseDemonstrativo(String json, String tipo) {
        if (json == null || json.isBlank()) {
            return BdrDemonstrativoDTO.empty(tipo);
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            return new BdrDemonstrativoDTO(tipo, root);
        } catch (Exception ex) {
            logger.warn("Erro ao parsear demonstrativo {} de BDR: {}", tipo, ex.getMessage());
            return BdrDemonstrativoDTO.empty(tipo);
        }
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
