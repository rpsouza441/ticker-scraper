package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InfoHeader(
        @JsonProperty("ticker")
        String ticker,

        @JsonProperty("nomeBdr")
        String nomeBdr
) {}