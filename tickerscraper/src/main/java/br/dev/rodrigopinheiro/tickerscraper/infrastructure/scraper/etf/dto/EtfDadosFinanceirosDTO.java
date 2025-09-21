package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.dto;

/**
 * DTO principal para dados financeiros de um ETF.
 * Agrega informações do header e dos cards de dados.
 */
public record EtfDadosFinanceirosDTO(
        EtfInfoHeaderDTO infoHeader,
        EtfInfoCardsDTO infoCards
) {
}