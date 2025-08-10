package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record FiiInfoSobreDTO(
        @JsonProperty("RAZAO SOCIAL")
        @JsonAlias({"razao_social"})
        String razaoSocial,

        @JsonProperty("CNPJ")
        @JsonAlias({"cnpj"})
        String cnpj,

        @JsonProperty("PUBLICO-ALVO")
        @JsonAlias({"publicoalvo"})
        String publicoAlvo,

        @JsonProperty("MANDATO")
        @JsonAlias({"mandato"})
        String mandato,

        @JsonProperty("SEGMENTO")
        @JsonAlias({"segmento"})
        String segmento,

        @JsonProperty("TIPO DE FUNDO")
        @JsonAlias({"tipo_de_fundo"})
        String tipoDeFundo,

        @JsonProperty("PRAZO DE DURACAO")
        @JsonAlias({"prazo_de_duracao"})
        String prazoDeDuracao,

        @JsonProperty("TIPO DE GESTAO")
        @JsonAlias({"tipo_de_gestao"})
        String tipoDeGestao,

        @JsonProperty("TAXA DE ADMINISTRACAO")
        @JsonAlias({"taxa_de_administracao"})
        String taxaDeAdministracao,

        @JsonProperty("VACANCIA")
        @JsonAlias({"vacancia"})
        String vacancia,

        @JsonProperty("NUMERO DE COTISTAS")
        @JsonAlias({"numero_de_cotistas"})
        String numeroDeCotistas,

        @JsonProperty("COTAS EMITIDAS")
        @JsonAlias({"cotas_emitidas"})
        String cotasEmitidas,

        @JsonProperty("VAL. PATRIMONIAL P/ COTA")
        @JsonAlias({"val_patrimonial_p_cota"})
        String valorPatrimonialPorCota,

        @JsonProperty("VALOR PATRIMONIAL")
        @JsonAlias({"valor_patrimonial"})
        String valorPatrimonial,

        @JsonProperty("ULTIMO RENDIMENTO")
        @JsonAlias({"ultimo_rendimento"})
        String ultimoRendimento
) {}
