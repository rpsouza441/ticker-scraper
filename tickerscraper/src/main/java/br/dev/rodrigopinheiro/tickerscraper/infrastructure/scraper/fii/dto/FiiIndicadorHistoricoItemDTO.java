package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record FiiIndicadorHistoricoItemDTO(
        @JsonProperty("year")
        String year,

        @JsonProperty("key")
        String key,

        @JsonProperty("value")
        BigDecimal value,

        @JsonProperty("type")
        String type
) {
}
