package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AcaoInfoHeaderDTO(
        @JsonProperty("ticker")
        String ticker,
        @JsonProperty("nome_empresa")
        String nomeEmpresa
) {
}
