package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para capturar informações dos cards de dados financeiros de um ETF.
 * Baseado na estrutura HTML da seção cards-ticker.
 */
public record EtfInfoCardsDTO(
        @JsonProperty("Valor atual")
        String valorAtual,
        
        @JsonProperty("Capitalização")
        String capitalizacao,
        
        @JsonProperty("VARIAÇÃO (12M)")
        String variacao12M,
        
        @JsonProperty("VARIAÇÃO (60M)")
        String variacao60M,
        
        @JsonProperty("DY")
        String dy
) {
}