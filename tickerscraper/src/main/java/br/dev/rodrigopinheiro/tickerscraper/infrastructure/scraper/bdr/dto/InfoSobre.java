package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InfoSobre(
        @JsonProperty("marketCapText")
        String marketCapText,

        @JsonProperty("setor")
        String setor,

        @JsonProperty("industria")
        String industria,

        @JsonProperty("paridadeText")
        String paridadeText
) {}