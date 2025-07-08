package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto;


public record AcaoDadosFinanceiros(
        AcaoInfoHeader infoHeader,
        AcaoInfoDetailed infoDetailed,
        AcaoInfoCards infoCards,
        AcaoIndicadoresFundamentalistas fundamentalIndicators
) {}