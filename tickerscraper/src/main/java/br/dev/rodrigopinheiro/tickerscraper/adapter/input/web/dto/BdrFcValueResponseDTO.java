package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrFcValueResponseDTO(
        BigDecimal valor,
        String quality,
        String raw
) {
}
