package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BdrPricePointResponseDTO(
        LocalDate dt,
        BigDecimal close
) {
}
