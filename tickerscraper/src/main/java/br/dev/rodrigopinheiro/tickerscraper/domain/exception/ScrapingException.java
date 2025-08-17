package br.dev.rodrigopinheiro.tickerscraper.domain.exception;

/**
 * Exception base para todas as operações de scraping.
 * Contém informações contextuais comuns como ticker e URL.
 * 
 * @author Sistema de Scraping
 * @since 1.0
 */
public abstract class ScrapingException extends RuntimeException {
    
    private final String ticker;
    private final String url;
    private final String operation;
    
    /**
     * Construtor base para exceções de scraping.
     * 
     * @param message Mensagem descritiva do erro
     * @param ticker Código do ticker sendo processado
     * @param url URL que estava sendo acessada
     * @param operation Operação sendo executada (scraping, parsing, etc.)
     * @param cause Causa raiz da exceção (opcional)
     */
    protected ScrapingException(String message, String ticker, String url, String operation, Throwable cause) {
        super(formatMessage(message, ticker, url, operation), cause);
        this.ticker = ticker;
        this.url = url;
        this.operation = operation;
    }
    
    /**
     * Construtor simplificado sem causa raiz.
     */
    protected ScrapingException(String message, String ticker, String url, String operation) {
        this(message, ticker, url, operation, null);
    }
    
    private static String formatMessage(String message, String ticker, String url, String operation) {
        return String.format("[%s] %s - Ticker: %s, URL: %s", 
                           operation != null ? operation.toUpperCase() : "SCRAPING", 
                           message, 
                           ticker != null ? ticker : "N/A", 
                           url != null ? url : "N/A");
    }
    
    public String getTicker() {
        return ticker;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getOperation() {
        return operation;
    }
    
    /**
     * Retorna código de erro específico para cada tipo de exceção.
     * Deve ser implementado pelas subclasses.
     */
    public abstract String getErrorCode();
    
    /**
     * Indica se a operação pode ser tentada novamente.
     * Por padrão, retorna true, mas pode ser sobrescrito.
     */
    public boolean isRetryable() {
        return true;
    }
}