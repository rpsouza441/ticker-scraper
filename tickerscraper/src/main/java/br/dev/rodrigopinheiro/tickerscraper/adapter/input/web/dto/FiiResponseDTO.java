package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FiiResponseDTO(
        String ticker,
        TipoAtivo tipoAtivo,
        String nomeEmpresa,
        String razaoSocial,
        String cnpj,
        String publicoAlvo,
        String mandato,
        String segmento,
        String tipoDeFundo,
        String prazoDeDuracao,
        String tipoDeGestao,

        BigDecimal taxaDeAdministracao,
        BigDecimal ultimoRendimento,

        BigDecimal cotacao,
        BigDecimal variacao12M,

        BigDecimal valorDeMercado,
        BigDecimal pvp,
        BigDecimal dividendYield,
        BigDecimal liquidezDiaria,
        BigDecimal valorPatrimonial,
        BigDecimal valorPatrimonialPorCota,
        BigDecimal vacancia,

        Long numeroDeCotistas,
        Long cotasEmitidas,

        LocalDateTime dataAtualizacao,
        List<FiiDividendoResponseDTO> dividendos
) {}
