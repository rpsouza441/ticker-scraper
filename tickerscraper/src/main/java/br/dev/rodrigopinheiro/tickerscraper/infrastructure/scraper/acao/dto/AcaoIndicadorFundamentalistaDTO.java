package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AcaoIndicadorFundamentalistaDTO(
        @JsonProperty("valor")
        String valor,

        @JsonProperty("definicao")
        String definicao,

        @JsonProperty("calculo")
        String calculo,

        @JsonProperty("Setor")
        String setor,

        @JsonProperty("Subsetor")
        String subsetor,

        @JsonProperty("Segmento")
        String segmento
) {}
