package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO consolidando os indicadores correntes e históricos do BDR.
 */
public record BdrIndicadoresDTO(
        Map<String, BigDecimal> indicadoresMonetarios,
        Map<String, BigDecimal> indicadoresPercentuais,
        Map<String, Double> indicadoresSimples,
        IndicadorParser.ParidadeBdrInfo paridade,
        String moedaPadrao,
        JsonNode raw
) {
    public static BdrIndicadoresDTO empty() {
        return new BdrIndicadoresDTO(Map.of(), Map.of(), Map.of(), null, null, null);
    }
}
