package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;


import java.util.List;

public record FiiDadosFinanceirosDTO(
        FiiInfoHeaderDTO infoHeaderDTO,
        FiiIndicadorHistoricoDTO infoHistoricoDTO,
        FiiInfoSobreDTO infoSobreDTO,
        List<FiiDividendoDTO> dividendoDTOList,
        FiiCotacaoDTO cotacaoDTO
) {
}