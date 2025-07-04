package br.dev.rodrigopinheiro.tickerscraper.domain.model;


public record AcaoDadosFinanceiros(
        AcaoInfoHeader infoHeader,
        AcaoInfoDetailed infoDetailed,
        AcaoInfoCards  infoCards,
        AcaoIndicadoresFundamentalistas fundamentalIndicators
) {}