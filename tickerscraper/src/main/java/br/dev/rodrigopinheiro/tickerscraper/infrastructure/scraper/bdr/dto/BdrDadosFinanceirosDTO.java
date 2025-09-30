package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BdrDadosFinanceirosDTO(
        @JsonProperty("infoHeader")
        InfoHeader infoHeader,

        @JsonProperty("infoCards")
        InfoCards infoCards,

        @JsonProperty("infoSobre")
        InfoSobre infoSobre,

        @JsonProperty("indicadores")
        Map<String, Object> indicadores,

        @JsonProperty("demonstrativos")
        Demonstrativos demonstrativos,

        @JsonProperty("dividendos")
        Map<String, Object> dividendos,

        @JsonProperty("updatedAt")
        Instant updatedAt
) {





}