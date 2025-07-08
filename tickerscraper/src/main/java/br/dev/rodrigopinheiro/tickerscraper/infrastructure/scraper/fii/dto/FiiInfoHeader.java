package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FiiInfoHeader(
        @JsonProperty("ticker")
        String ticker,
        @JsonProperty("nome_empresa")
        String nomeEmpresa
) {
}
