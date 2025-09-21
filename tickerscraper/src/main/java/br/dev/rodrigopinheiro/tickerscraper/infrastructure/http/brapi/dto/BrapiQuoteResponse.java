package br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response da API Brapi para consulta de cotações.
 * 
 * @see <a href="https://brapi.dev/docs/quote">Documentação Brapi</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BrapiQuoteResponse(
    @JsonProperty("results")
    List<BrapiQuoteResult> results,
    
    @JsonProperty("requestedAt")
    String requestedAt,
    
    @JsonProperty("took")
    String took
) {
    
    /**
     * Verifica se a resposta contém resultados válidos.
     */
    public boolean hasResults() {
        return results != null && !results.isEmpty();
    }
    
    /**
     * Obtém o primeiro resultado, se disponível.
     */
    public BrapiQuoteResult getFirstResult() {
        return hasResults() ? results.get(0) : null;
    }
}