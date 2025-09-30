package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BdrApiScraper {
    private static final Logger logger = LoggerFactory.getLogger(BdrApiScraper.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BdrApiScraper(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Map<String, Object>> fetchIndicadores(String url, Map<String, String> headers) {
        logger.info("Calling BDR Indicators API: {}", url);
        return webClient.get().uri(url)
                .headers(h -> h.setAll(headers))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorReturn(Collections.emptyMap());
    }

    public Mono<Map<String, Object>> fetchDividendos(String url, Map<String, String> headers) {
        logger.info("Calling BDR Dividends API: {}", url);
        return webClient.get().uri(url)
                .headers(h -> h.setAll(headers))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .map(list -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("content", list);
                    return map;
                })
                .onErrorReturn(Collections.emptyMap());
    }

    public Mono<Map<String, Object>> fetchDre(String url, Map<String, String> headers) {
        return fetchAndParseFinancialStatement(url, headers, "DRE");
    }

    public Mono<Map<String, Object>> fetchBalancoPatrimonial(String url, Map<String, String> headers) {
        return fetchAndParseFinancialStatement(url, headers, "Balance Sheet");
    }

    public Mono<Map<String, Object>> fetchFluxoCaixa(String url, Map<String, String> headers) {
        return fetchAndParseFinancialStatement(url, headers, "Cash Flow");
    }

    private Mono<Map<String, Object>> fetchAndParseFinancialStatement(String url, Map<String, String> headers, String type) {
        logger.info("Calling Financial Statement API ({}): {}", type, url);
        return webClient.get().uri(url)
                .headers(h -> h.setAll(headers))
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseFinancialStatementJson)
                .doOnError(e -> logger.error("Failed to process statement '{}' from API {}: {}", type, url, e.getMessage()))
                .onErrorReturn(Collections.emptyMap());
    }

    /**
     * Correctly parses the tabular JSON (Array of Arrays) into a structured Map.
     * Example: [["Revenue", "100", "110"]] -> {"Revenue": {"ÚLT. 12M": "100", "2024": "110"}}
     */
    private Map<String, Object> parseFinancialStatementJson(String jsonString) {
        try {
            List<List<Object>> data = objectMapper.readValue(jsonString, new TypeReference<>() {});
            if (data == null || data.size() < 2) {
                return Collections.emptyMap();
            }

            List<Object> header = data.get(0); // ["#", "ÚLT. 12M", "2024", ...]
            Map<String, Object> result = new LinkedHashMap<>();

            for (int i = 1; i < data.size(); i++) {
                List<Object> row = data.get(i);
                String rowLabel = (String) row.get(0);
                Map<String, Object> values = new LinkedHashMap<>();

                for (int j = 1; j < row.size() && j < header.size(); j++) {
                    String period = String.valueOf(header.get(j));
                    Object rawValue = row.get(j);

                    // The value can be a List ["128,83 Bilhões", "128825000000,00"] or a String "23,68%"
                    if (rawValue instanceof List && !((List<?>) rawValue).isEmpty()) {
                        values.put(period, ((List<?>) rawValue).get(0));
                    } else {
                        values.put(period, rawValue);
                    }
                }
                result.put(rowLabel, values);
            }
            return result;
        } catch (Exception e) {
            logger.error("Error parsing financial statement JSON: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}