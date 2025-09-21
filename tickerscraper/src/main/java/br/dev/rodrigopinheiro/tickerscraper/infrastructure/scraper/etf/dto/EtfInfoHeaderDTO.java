package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para capturar informações do header de um ETF.
 * Baseado na estrutura HTML da seção header_action.
 */
public record EtfInfoHeaderDTO(
        @JsonProperty("ticker")
        String ticker,
        
        @JsonProperty("nomeEtf")
        String nomeEtf
) {
}