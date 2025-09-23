package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrFcYearResponseDTO(
        Integer ano,
        BigDecimal fluxoCaixaOperacional,
        BigDecimal fluxoCaixaInvestimento,
        BigDecimal fluxoCaixaFinanciamento,
        BigDecimal caixaFinal
) {
}
