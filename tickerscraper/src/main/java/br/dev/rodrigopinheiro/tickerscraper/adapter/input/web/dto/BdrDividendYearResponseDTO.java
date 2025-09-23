package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrDividendYearResponseDTO(
        Integer ano,
        BigDecimal totalDividendo,
        BigDecimal dividendYield
) {
}
