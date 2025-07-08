package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;
import java.util.Map;

public record FiiIndicadorHistoricoDTO(
        @JsonValue
        Map<String, List<FiiIndicadorHistoricoItemDTO>> indicadores
) {
}
