package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representa um ponto individual da série histórica de preços de um BDR.
 */
public record BdrPricePointDTO(
        Instant data,
        BigDecimal preco
) {
}
