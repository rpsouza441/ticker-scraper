package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record FiiIndicadorHistoricoItemDTO(
        @JsonProperty("year")
        String ano,

        @JsonProperty("key")
        String chave,

        @JsonProperty("value")
        BigDecimal valor,

        @JsonProperty("type")
        String tipo
) {
}
