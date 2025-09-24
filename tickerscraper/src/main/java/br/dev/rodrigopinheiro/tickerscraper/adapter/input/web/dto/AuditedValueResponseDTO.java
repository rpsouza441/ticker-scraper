package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record AuditedValueResponseDTO(
        BigDecimal value,
        String quality,
        String raw
) {
}
