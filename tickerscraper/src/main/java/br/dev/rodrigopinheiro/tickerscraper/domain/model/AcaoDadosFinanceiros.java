package br.dev.rodrigopinheiro.tickerscraper.domain.model;


public record AcaoDadosFinanceiros(
        InfoHeader infoHeader,
        AcaoInfoDetailed infoDetailed,
        AcaoInfoCards  infoCards,
        AcaoIndicadoresFundamentalistas fundamentalIndicators
) {}