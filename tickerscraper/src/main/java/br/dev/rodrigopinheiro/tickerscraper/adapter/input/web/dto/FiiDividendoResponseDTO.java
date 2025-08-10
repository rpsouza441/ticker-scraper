package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record FiiDividendoResponseDTO(
        String mes,
        BigDecimal valor
) {
}
