package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record BdrResponseDTO(
        String ticker,
        TipoAtivo tipoAtivo,
        String nomeEmpresa,
        String nomeAcaoOriginal,
        String codigoNegociacao,
        String mercado,
        String paisDeNegociacao,
        String moedaDeReferencia,
        BigDecimal precoAtual,
        BigDecimal variacaoDia,
        BigDecimal variacaoMes,
        BigDecimal variacaoAno,
        BigDecimal dividendYield,
        BigDecimal precoAlvo,
        BdrParidadeResponseDTO paridade,
        BdrCurrentIndicatorsResponseDTO indicadores,
        List<BdrPricePointResponseDTO> historicoDePrecos,
        List<BdrDividendYearResponseDTO> dividendosPorAno,
        List<BdrHistoricalIndicatorResponseDTO> indicadoresHistoricos,
        List<BdrDreYearResponseDTO> dreAnual,
        List<BdrBpYearResponseDTO> balancoPatrimonial,
        List<BdrFcYearResponseDTO> fluxoDeCaixa,
        Instant atualizadoEm,
        Map<String, Object> rawJson
) {
}
