package br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response de erro da API Brapi.
 * 
 * Exemplo: {"error": true, "message": "Não encontramos a ação KNH26Y1512"}
 * 
 * @see <a href="https://brapi.dev/docs/quote">Documentação Brapi</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BrapiErrorResponse(
    @JsonProperty("error")
    boolean error,
    
    @JsonProperty("message")
    String message
) {
    
    /**
     * Verifica se é uma resposta de erro.
     */
    public boolean isError() {
        return error;
    }
    
    /**
     * Verifica se é um erro de ticker não encontrado.
     */
    public boolean isTickerNotFound() {
        return error && message != null && 
               (message.toLowerCase().contains("não encontramos") ||
                message.toLowerCase().contains("not found"));
    }
}