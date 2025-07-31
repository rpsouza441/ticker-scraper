package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;


import java.util.List;

public record FiiDadosFinanceirosDTO(
        Integer internalId,
        FiiInfoHeaderDTO infoHeader,
        FiiIndicadorHistoricoDTO infoHistorico,
        FiiInfoSobreDTO infoSobre,
        FiiInfoCardsDTO infoCards,
        List<FiiDividendoDTO> dividendos,
        FiiCotacaoDTO cotacao
) {
}