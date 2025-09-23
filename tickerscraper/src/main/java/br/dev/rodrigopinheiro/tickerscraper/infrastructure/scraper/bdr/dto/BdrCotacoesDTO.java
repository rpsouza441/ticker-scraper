package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * DTO com a série histórica de preços capturada do endpoint de cotações.
 */
public record BdrCotacoesDTO(
        List<BdrPricePointDTO> serie,
        String moeda,
        JsonNode raw
) {
    public static BdrCotacoesDTO empty() {
        return new BdrCotacoesDTO(List.of(), null, null);
    }
}
