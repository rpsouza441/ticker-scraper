package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;

import java.math.BigDecimal;

/**
 * Representa um pagamento de dividendo agregado por período (ano/mês) para um BDR.
 */
public record BdrDividendoItemDTO(
        String periodo,
        BigDecimal valor,
        String moeda
) {
}
