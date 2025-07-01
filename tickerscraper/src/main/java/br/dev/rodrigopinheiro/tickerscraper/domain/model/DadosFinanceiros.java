package br.dev.rodrigopinheiro.tickerscraper.domain.model;


public record DadosFinanceiros(
        InfoHeader infoHeader,
        InfoDetailed infoDetailed,
        InfoCards  infoCards,
        IndicadoresFundamentalistas fundamentalIndicators
) {}