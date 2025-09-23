package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * DTO com os dividendos históricos capturados do endpoint dedicado.
 */
public record BdrDividendosDTO(
        List<BdrDividendoItemDTO> dividendos,
        JsonNode raw
) {
    public static BdrDividendosDTO empty() {
        return new BdrDividendosDTO(List.of(), null);
    }
}
