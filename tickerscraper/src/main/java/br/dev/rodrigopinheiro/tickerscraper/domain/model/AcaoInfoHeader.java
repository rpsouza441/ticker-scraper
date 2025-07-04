package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AcaoInfoHeader(
        @JsonProperty("ticker")
        String ticker,
        @JsonProperty("nome_empresa")
        String nomeEmpresa
) {
}
