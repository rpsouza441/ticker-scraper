package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.parser;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrDemonstrativoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Converte os payloads dos demonstrativos financeiros (DRE, BP, FC).
 */
@Component
public class BdrDemonstrativosParser {

    private static final Logger logger = LoggerFactory.getLogger(BdrDemonstrativosParser.class);

    private final ObjectMapper objectMapper;

    public BdrDemonstrativosParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BdrDemonstrativoDTO parse(String json, String tipo) {
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
}
