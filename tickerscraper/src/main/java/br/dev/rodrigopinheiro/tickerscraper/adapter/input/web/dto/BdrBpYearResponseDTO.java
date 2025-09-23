package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrBpYearResponseDTO(
        Integer ano,
        BigDecimal ativosTotais,
        BigDecimal passivosTotais,
        BigDecimal patrimonioLiquido,
        BigDecimal caixaEDisponibilidades,
        BigDecimal dividaBruta
) {
}
