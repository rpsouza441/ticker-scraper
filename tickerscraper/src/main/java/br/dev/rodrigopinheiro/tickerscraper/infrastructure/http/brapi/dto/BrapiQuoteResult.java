package br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;

/**
 * Resultado individual de uma consulta de cotação na API Brapi.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BrapiQuoteResult(
    @JsonProperty("symbol")
    String symbol,
    
    @JsonProperty("shortName")
    String shortName,
    
    @JsonProperty("longName")
    String longName,
    
    @JsonProperty("currency")
    String currency,
    
    @JsonProperty("regularMarketPrice")
    Double regularMarketPrice,
    
    @JsonProperty("marketCap")
    String marketCap,
    
    @JsonProperty("logourl")
    String logoUrl
) {
    
    /**
     * Verifica se o resultado é válido (tem símbolo).
     */
    public boolean isValid() {
        return symbol != null && !symbol.trim().isEmpty();
    }
    
    /**
     * Obtém o nome para exibição (shortName ou longName).
     */
    public String getDisplayName() {
        if (shortName != null && !shortName.trim().isEmpty()) {
            return shortName;
        }
        return longName != null ? longName : symbol;
    }
}