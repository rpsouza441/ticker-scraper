package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.parser;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrIndicadoresDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Parser responsável por consolidar os indicadores financeiros do BDR.
 */
@Component
public class BdrIndicadoresParser {

    private static final Logger logger = LoggerFactory.getLogger(BdrIndicadoresParser.class);

    private final ObjectMapper objectMapper;

    public BdrIndicadoresParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BdrIndicadoresDTO parse(String json) {
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
}
