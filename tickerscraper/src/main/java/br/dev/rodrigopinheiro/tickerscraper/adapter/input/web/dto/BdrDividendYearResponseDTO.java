package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrDividendYearResponseDTO(
        Integer year,
        BigDecimal valor,
        BigDecimal dividendYield,
        String currency
) {
}
