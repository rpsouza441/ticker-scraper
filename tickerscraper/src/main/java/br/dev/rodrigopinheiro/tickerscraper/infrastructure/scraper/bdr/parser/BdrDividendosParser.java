package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.parser;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrDividendosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrDividendoItemDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Converte os payloads de dividendos em {@link BdrDividendosDTO}.
 */
@Component
public class BdrDividendosParser {

    private static final Logger logger = LoggerFactory.getLogger(BdrDividendosParser.class);

    private final ObjectMapper objectMapper;

    public BdrDividendosParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BdrDividendosDTO parse(String json) {
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
                BigDecimal valor = parseValor(item);
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

    private BigDecimal parseValor(JsonNode item) {
        if (item.has("valor")) {
            return IndicadorParser.parseValorMonetario(item.get("valor").asText()).orElse(null);
        }
        if (item.has("value")) {
            JsonNode valueNode = item.get("value");
            if (valueNode.isNumber()) {
                return valueNode.decimalValue();
            }
            if (valueNode.isTextual()) {
                return IndicadorParser.parseValorMonetario(valueNode.asText()).orElse(null);
            }
        }
        return null;
    }
}
