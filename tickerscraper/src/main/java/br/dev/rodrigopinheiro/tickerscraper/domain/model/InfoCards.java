package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InfoCards(
        @JsonProperty("cotacao")
        String cotacao,
        @JsonProperty("variacao12M")
        String variacao12M
) {
}
