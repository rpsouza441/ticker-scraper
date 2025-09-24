package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrCurrentIndicatorsResponseDTO(
        BigDecimal pl,
        BigDecimal pvp,
        BigDecimal psr,
        BigDecimal pEbit,
        BigDecimal pEbitda,
        BigDecimal pAtivo,
        BigDecimal roe,
        BigDecimal roic,
        BigDecimal roa,
        BdrCurrentMarginsResponseDTO margens,
        BigDecimal vpa,
        BigDecimal lpa,
        BigDecimal patrimonioPorAtivos
) {
}
