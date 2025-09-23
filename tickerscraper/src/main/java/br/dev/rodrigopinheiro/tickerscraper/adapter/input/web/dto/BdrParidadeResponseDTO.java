package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrParidadeResponseDTO(
        BigDecimal fatorConversao,
        String tickerOriginal,
        String bolsaOrigem,
        String moedaOrigem
) {
}
