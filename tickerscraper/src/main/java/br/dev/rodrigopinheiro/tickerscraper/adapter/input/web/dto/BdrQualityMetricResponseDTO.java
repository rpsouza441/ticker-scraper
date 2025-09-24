package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.Quality;

import java.math.BigDecimal;

public record BdrQualityMetricResponseDTO(
        BigDecimal value,
        Quality quality,
        String raw
) {
}
