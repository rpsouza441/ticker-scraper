package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Resposta padronizada para erros da API.
 * Fornece informações estruturadas sobre falhas para facilitar debugging e monitoramento.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,                    // Código do erro (SCRAPING_TIMEOUT, TICKER_NOT_FOUND, etc.)
        String message,                 // Mensagem amigável para o usuário
        String ticker,                  // Ticker relacionado ao erro (se aplicável)
        String correlationId,           // ID para rastreamento de logs
        LocalDateTime timestamp,        // Momento do erro
        Boolean retryable,              // Indica se a operação pode ser tentada novamente
        Map<String, Object> details     // Detalhes técnicos adicionais
) {
    
    /**
     * Builder para facilitar criação de ErrorResponse.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String code;
        private String message;
        private String ticker;
        private String correlationId;
        private LocalDateTime timestamp;
        private Boolean retryable;
        private Map<String, Object> details;
        
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder ticker(String ticker) {
            this.ticker = ticker;
            return this;
        }
        
        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder retryable(Boolean retryable) {
            this.retryable = retryable;
            return this;
        }
        
        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }
        
        public ErrorResponse build() {
            return new ErrorResponse(code, message, ticker, correlationId, timestamp, retryable, details);
        }
    }
    
    /**
     * Cria um ErrorResponse simples com apenas código e mensagem.
     */
    public static ErrorResponse simple(String code, String message) {
        return new ErrorResponse(code, message, null, null, LocalDateTime.now(), null, null);
    }
    
    /**
     * Cria um ErrorResponse para ticker específico.
     */
    public static ErrorResponse forTicker(String code, String message, String ticker) {
        return new ErrorResponse(code, message, ticker, null, LocalDateTime.now(), null, null);
    }
}