package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrCurrentIndicatorsResponseDTO(
        BigDecimal pl,
        BigDecimal pvp,
        BigDecimal psr,
        BigDecimal pEbit,
        BigDecimal roe,
        BdrCurrentMarginsResponseDTO margens,
        BigDecimal vpa,
        BigDecimal lpa,
        BigDecimal patrimonioPorAtivos
) {
}
