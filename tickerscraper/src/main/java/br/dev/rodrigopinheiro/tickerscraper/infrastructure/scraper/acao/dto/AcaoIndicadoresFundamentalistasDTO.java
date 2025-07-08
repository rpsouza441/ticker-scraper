package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record AcaoIndicadoresFundamentalistasDTO(
        @JsonProperty("indicadores_fundamentalistas")
        Map<String, AcaoIndicadorFundamentalistaDTO> indicadores
) {}
