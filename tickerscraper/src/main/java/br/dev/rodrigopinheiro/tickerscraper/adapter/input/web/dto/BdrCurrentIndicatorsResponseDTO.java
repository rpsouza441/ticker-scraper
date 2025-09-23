package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrCurrentIndicatorsResponseDTO(
        BigDecimal ultimoPreco,
        BigDecimal variacaoPercentualDia,
        BigDecimal variacaoPercentualMes,
        BigDecimal variacaoPercentualAno,
        BigDecimal dividendYield,
        BigDecimal precoLucro,
        BigDecimal precoValorPatrimonial,
        BigDecimal valorMercado,
        BigDecimal volumeMedio
) {
}
