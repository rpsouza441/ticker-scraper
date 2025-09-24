package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrCurrentMarginsResponseDTO(
        BigDecimal margemBruta,
        BigDecimal margemEbit,
        BigDecimal margemLiquida
) {
}
