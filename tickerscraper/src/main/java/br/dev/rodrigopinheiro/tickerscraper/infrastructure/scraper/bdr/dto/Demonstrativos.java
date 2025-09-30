package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Demonstrativos(
        @JsonProperty("dre")
        Map<String, Object> dre,

        @JsonProperty("bp")
        Map<String, Object> bp,

        @JsonProperty("fc")
        Map<String, Object> fc
) {}