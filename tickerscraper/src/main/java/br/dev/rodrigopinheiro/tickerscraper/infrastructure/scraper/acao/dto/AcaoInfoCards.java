package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AcaoInfoCards(
        @JsonProperty("cotacao")
        String cotacao,
        @JsonProperty("variacao12M")
        String variacao12M
) {
}
