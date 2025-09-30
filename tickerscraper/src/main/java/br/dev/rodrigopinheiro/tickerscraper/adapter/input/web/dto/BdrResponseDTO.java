package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO principal para a resposta da API de dados processados de um BDR.
 * Este é o objeto que será serializado para JSON.
 */
public record BdrResponseDTO(
        String ticker,
        TipoAtivo tipoAtivo,
        String nome,
        BigDecimal precoAtual,
        Double variacao12M,
        BigDecimal dividendYield,
        Instant dataAtualizacao,
        String setor,
        String industria,
        String priceCurrency,
        String financialsCurrency,
        BigDecimal marketCapValue,
        String marketCapCurrency,
        BigDecimal paridadeRatio,
        BigDecimal pl,
        BigDecimal pvp,
        BigDecimal psr,
        BigDecimal pEbit,
        BigDecimal pEbitda,
        BigDecimal pAtivo,
        BigDecimal roe,
        BigDecimal roic,
        BigDecimal roa,
        BigDecimal margemBruta,
        BigDecimal margemOperacional,
        BigDecimal margemLiquida,
        BigDecimal vpa,
        BigDecimal lpa,
        BigDecimal receitaTotalUsd,
        BigDecimal lucroBrutoUsd,
        BigDecimal ebitdaUsd,
        BigDecimal ebitUsd,
        BigDecimal lucroLiquidoUsd,
        BigDecimal ativosTotaisUsd,
        BigDecimal passivosTotaisUsd,
        BigDecimal dividaLpUsd,
        BigDecimal plUsd,
        BigDecimal fcoUsd,
        BigDecimal fciUsd,
        BigDecimal fcfUsd,
        List<BdrDividendoResponseDTO> dividendos
) {}
