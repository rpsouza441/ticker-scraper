package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AcaoInfoCardsDTO(
        @JsonProperty("cotacao")
        String cotacao,
        @JsonProperty("variacao12M")
        String variacao12M
) {
}
