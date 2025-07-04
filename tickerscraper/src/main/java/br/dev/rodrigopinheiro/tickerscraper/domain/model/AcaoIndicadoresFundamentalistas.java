package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record AcaoIndicadoresFundamentalistas(
        @JsonProperty("indicadores_fundamentalistas")
        Map<String, AcaoIndicadorFundamentalista> indicadores
) {}
