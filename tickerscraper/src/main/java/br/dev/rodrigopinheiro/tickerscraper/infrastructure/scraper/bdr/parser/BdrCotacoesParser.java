package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.parser;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrCotacoesDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrPricePointDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Responsável por converter o payload bruto de cotações em um {@link BdrCotacoesDTO}.
 */
@Component
public class BdrCotacoesParser {

    private static final Logger logger = LoggerFactory.getLogger(BdrCotacoesParser.class);

    private final ObjectMapper objectMapper;

    public BdrCotacoesParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BdrCotacoesDTO parse(String json) {
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
}
