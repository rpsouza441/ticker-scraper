package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AcaoInfoDetailedDTO(

        @JsonProperty("Valor de mercado")
        String valorMercado,

        @JsonProperty("Valor de firma")
        String valorFirma,

        @JsonProperty("Patrimônio Líquido")
        String patrimonioLiquido,

        @JsonProperty("Nº total de papeis")
        String numeroTotalPapeis,

        @JsonProperty("Ativos")
        String ativos,

        @JsonProperty("Ativo Circulante")
        String ativoCirculante,

        @JsonProperty("Dívida Bruta")
        String dividaBruta,

        @JsonProperty("Dívida Líquida")
        String dividaLiquida,

        @JsonProperty("Disponibilidade")
        String disponibilidade,

        @JsonProperty("Segmento de Listagem")
        String segmentoListagem,

        @JsonProperty("Free Float")
        String freeFloat,

        @JsonProperty("Tag Along")
        String tagAlong,

        @JsonProperty("Liquidez Média Diária")
        String liquidezMediaDiaria,

        @JsonProperty("Setor")
        String setor,

        @JsonProperty("Segmento")
        String segmento
){
}
