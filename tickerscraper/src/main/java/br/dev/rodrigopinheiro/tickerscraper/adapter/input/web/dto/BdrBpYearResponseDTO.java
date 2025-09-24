package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

public record BdrBpYearResponseDTO(
        Integer ano,
        AuditedValueResponseDTO ativosTotais,
        AuditedValueResponseDTO passivosTotais,
        AuditedValueResponseDTO dividaLongoPrazo,
        AuditedValueResponseDTO pl
) {
}
