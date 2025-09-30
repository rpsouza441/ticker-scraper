package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;

public record BdrDividendoResponseDTO(
        String mes,      // Formato "MM/yyyy"
        BigDecimal valor,
        String moeda    // Ex: "USD"
) {}