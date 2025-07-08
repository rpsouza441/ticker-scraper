package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FiiInfoCards(
        @JsonProperty("cotacao")
        String cotacao,
        @JsonProperty("variacao12M")
        String variacao12M
) {
}
