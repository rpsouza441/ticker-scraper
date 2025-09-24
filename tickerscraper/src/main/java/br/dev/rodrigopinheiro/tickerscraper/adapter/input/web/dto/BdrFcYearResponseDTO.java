package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

public record BdrFcYearResponseDTO(
        Integer ano,
        BdrFcValueResponseDTO fluxoCaixaOperacional,
        BdrFcValueResponseDTO fluxoCaixaInvestimento,
        BdrFcValueResponseDTO fluxoCaixaFinanciamento
) {
}
