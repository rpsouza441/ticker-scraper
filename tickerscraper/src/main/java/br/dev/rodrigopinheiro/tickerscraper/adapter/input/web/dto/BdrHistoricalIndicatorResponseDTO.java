package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrHistoricalIndicatorResponseDTO(
        String indicador,
        Integer ano,
        BigDecimal valor
) {
}
