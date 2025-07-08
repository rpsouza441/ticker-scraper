package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto;


public record AcaoDadosFinanceirosDTO(
        AcaoInfoHeaderDTO infoHeader,
        AcaoInfoDetailedDTO infoDetailed,
        AcaoInfoCardsDTO infoCards,
        AcaoIndicadoresFundamentalistasDTO fundamentalIndicators
) {}