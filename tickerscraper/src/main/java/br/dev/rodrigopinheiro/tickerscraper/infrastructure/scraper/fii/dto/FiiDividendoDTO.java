package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.YearMonth;

public record FiiDividendoDTO(
        @JsonProperty("price")
        BigDecimal price,

        // Jackson vai converter a String "MM/yyyy" para um objeto YearMonth
        @JsonProperty("created_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/yyyy")
        YearMonth created_at
) {
}
