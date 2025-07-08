package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FiiInfoSobreDTO(
        @JsonProperty("Razão Social")
        String razaoSocial,

        @JsonProperty("CNPJ")
        String cnpj,

        @JsonProperty("PÚBLICO-ALVO")
        String publicoAlvo,

        @JsonProperty("MANDATO")
        String mandato,

        @JsonProperty("SEGMENTO")
        String segmento,

        @JsonProperty("TIPO DE FUNDO")
        String tipoDeFundo,

        @JsonProperty("PRAZO DE DURAÇÃO")
        String prazoDeDuracao,

        @JsonProperty("TIPO DE GESTÃO")
        String tipoDeGestao,

        @JsonProperty("TAXA DE ADMINISTRAÇÃO")
        String taxaDeAdministracao,

        @JsonProperty("VACÂNCIA")
        String vacancia,

        @JsonProperty("NÚMERO DE COTISTAS")
        String numeroDeCotistas,

        @JsonProperty("COTAS EMITIDAS")
        String cotasEmitidas,

        @JsonProperty("VAL. PATRIMONIAL P/ COTA")
        String valorPatrimonialPorCota,

        @JsonProperty("VALOR PATRIMONIAL")
        String valorPatrimonial,

        @JsonProperty("ÚLTIMO RENDIMENTO")
        String ultimoRendimento
) {}