package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InfoCards(
        @JsonProperty("cotacao")
        BigDecimal cotacao,

        @JsonProperty("variacao12M")
        Double variacao12M
) {}