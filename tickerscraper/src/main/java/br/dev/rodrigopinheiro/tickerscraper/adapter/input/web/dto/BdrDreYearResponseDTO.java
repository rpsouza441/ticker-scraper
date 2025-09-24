package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

public record BdrDreYearResponseDTO(
        Integer ano,
        BdrQualityMetricResponseDTO receitaTotalUsd,
        BdrQualityMetricResponseDTO lucroBrutoUsd,
        BdrQualityMetricResponseDTO ebitdaUsd,
        BdrQualityMetricResponseDTO ebitUsd,
        BdrQualityMetricResponseDTO lucroLiquidoUsd
) {
}
