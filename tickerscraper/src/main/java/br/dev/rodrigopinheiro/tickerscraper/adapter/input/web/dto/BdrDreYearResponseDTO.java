package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrDreYearResponseDTO(
        Integer ano,
        BigDecimal receitaLiquida,
        BigDecimal lucroLiquido,
        BigDecimal ebitda,
        BigDecimal ebit,
        BigDecimal margemLiquida
) {
}
