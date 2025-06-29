package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record IndicadoresFundamentalistas(
        @JsonProperty("indicadores_fundamentalistas")
        Map<String, IndicadorFundamentalista> indicadores
) {}
