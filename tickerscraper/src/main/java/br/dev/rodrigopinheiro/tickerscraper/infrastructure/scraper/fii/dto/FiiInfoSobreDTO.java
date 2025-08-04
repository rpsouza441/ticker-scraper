package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FiiInfoSobreDTO(
        @JsonProperty("RAZAO SOCIAL")
        String razaoSocial,

        @JsonProperty("CNPJ")
        String cnpj,

        @JsonProperty("PUBLICO-ALVO")
        String publicoAlvo,

        @JsonProperty("MANDATO")
        String mandato,

        @JsonProperty("SEGMENTO")
        String segmento,

        @JsonProperty("TIPO DE FUNDO")
        String tipoDeFundo,

        @JsonProperty("PRAZO DE DURACAO")
        String prazoDeDuracao,

        @JsonProperty("TIPO DE GESTAO")
        String tipoDeGestao,

        @JsonProperty("TAXA DE ADMINISTRACAO")
        String taxaDeAdministracao,

        @JsonProperty("VACANCIA")
        String vacancia,

        @JsonProperty("NUMERO DE COTISTAS")
        String numeroDeCotistas,

        @JsonProperty("COTAS EMITIDAS")
        String cotasEmitidas,

        @JsonProperty("VAL. PATRIMONIAL P/ COTA")
        String valorPatrimonialPorCota,

        @JsonProperty("VALOR PATRIMONIAL")
        String valorPatrimonial,

        @JsonProperty("ULTIMO RENDIMENTO")
        String ultimoRendimento
) {}