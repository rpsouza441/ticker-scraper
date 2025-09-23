package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.network;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrApiConstants.BALANCO_DRE_PATH;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrApiConstants.COTACOES_CHART_PATH;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrApiConstants.DIVIDENDOS_PATH;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrApiConstants.HISTORICO_INDICADORES_PATH;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrApiConstants.KEY_BP;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrApiConstants.KEY_COTACOES;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrApiConstants.KEY_DIVIDENDOS;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrApiConstants.KEY_DRE;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrApiConstants.KEY_FC;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrApiConstants.KEY_INDICADORES;
import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrScraperConstants.NETWORK_CAPTURE_TIMEOUT_MS;

/**
 * Encapsula a configuração dos listeners de rede do Playwright e aguarda os payloads essenciais.
 */
@Component
public class BdrNetworkPayloadCollector {

    private static final Logger logger = LoggerFactory.getLogger(BdrNetworkPayloadCollector.class);

    private static final Pattern INVESTIDOR_ID_PATTERN = Pattern.compile("/chart/(\\d+)/");
    private static final Pattern BALANCO_TIPO_PATTERN = Pattern.compile("/balancos/(\\d+)/(DRE|BP|FC)/", Pattern.CASE_INSENSITIVE);

    public CaptureSession start(Page page) {
        ConcurrentHashMap<String, String> payloads = new ConcurrentHashMap<>();
        AtomicReference<String> investidorId = new AtomicReference<>();
        page.onResponse(response -> handleResponse(response, payloads, investidorId));
        return new CaptureSession(payloads, investidorId);
    }

    public BdrNetworkPayloadDTO awaitAndCollect(CaptureSession session, String ticker, String correlationId) {
        for (String key : new String[]{KEY_COTACOES, KEY_DIVIDENDOS, KEY_INDICADORES, KEY_DRE, KEY_BP, KEY_FC}) {
            waitForPayload(session.payloads(), key, ticker, correlationId);
        }
        return new BdrNetworkPayloadDTO(session.investidorId().get(), Map.copyOf(session.payloads()));
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

    public record CaptureSession(Map<String, String> payloads, AtomicReference<String> investidorId) {
    }
}
