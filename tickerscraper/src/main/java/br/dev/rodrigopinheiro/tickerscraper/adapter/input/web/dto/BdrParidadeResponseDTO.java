package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.ParidadeMethod;

import java.math.BigDecimal;
import java.time.Instant;

public record BdrParidadeResponseDTO(
        Integer ratio,
        ParidadeMethod method,
        BigDecimal confidence,
        Instant lastVerifiedAt,
        String raw
) {
}
