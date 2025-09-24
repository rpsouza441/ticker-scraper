package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrHistoricalIndicatorResponseDTO(
        Integer year,
        BigDecimal pl,
        BigDecimal pvp,
        BigDecimal psr,
        BigDecimal pEbit,
        BigDecimal pEbitda,
        BigDecimal pAtivo,
        BigDecimal roe,
        BigDecimal roic,
        BigDecimal roa,
        BigDecimal margemBruta,
        BigDecimal margemOperacional,
        BigDecimal margemLiquida,
        BigDecimal vpa,
        BigDecimal lpa,
        BigDecimal patrimonioPorAtivos
) {
}
