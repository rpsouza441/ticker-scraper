package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BdrPricePointResponseDTO(
        Instant timestamp,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal closePrice,
        BigDecimal volume
) {
}
