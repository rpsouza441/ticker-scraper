package br.dev.rodrigopinheiro.tickerscraper.domain.model;


public record DadosFinanceiros(
        InfoHeader headerInfo,
        InfoDetailed infoDetailed,
        InfoCards  infoCards,
        IndicadoresFundamentalistas fundamentalIndicators
) {}