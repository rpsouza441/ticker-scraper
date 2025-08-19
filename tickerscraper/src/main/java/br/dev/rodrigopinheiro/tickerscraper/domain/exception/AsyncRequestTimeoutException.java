package br.dev.rodrigopinheiro.tickerscraper.domain.exception;

import java.time.Duration;

/**
 * Exceção específica para timeouts de requisições assíncronas.
 * Ocorre quando operações assíncronas (como Playwright) excedem o tempo limite configurado.
 */
public class AsyncRequestTimeoutException extends ScrapingException {
    
    private final Duration timeout;
    private final String requestId;
    
    public AsyncRequestTimeoutException(String ticker, String operation, Duration timeout) {
        this(ticker, operation, timeout, null, null);
    }
    
    public AsyncRequestTimeoutException(String ticker, String operation, Duration timeout, String requestId) {
        this(ticker, operation, timeout, requestId, null);
    }
    
    public AsyncRequestTimeoutException(String ticker, String operation, Duration timeout, 
                                      String requestId, Throwable cause) {
        super(
            String.format("Timeout na operação assíncrona '%s' para ticker '%s' após %ds", 
                         operation, ticker, timeout.getSeconds()),
            ticker,
            null, // URL não disponível neste contexto
            operation,
            cause
        );
        this.timeout = timeout;
        this.requestId = requestId;
    }
    
    @Override
    public String getErrorCode() {
        return "ASYNC_TIMEOUT";
    }
    
    @Override
    public boolean isRetryable() {
        return true;
    }
    
    /**
     * Cria uma exceção para timeout de scraping Playwright.
     */
    public static AsyncRequestTimeoutException forPlaywrightScraping(String ticker, Duration timeout, String requestId) {
        return new AsyncRequestTimeoutException(ticker, "PLAYWRIGHT_SCRAPING", timeout, requestId);
    }
    
    /**
     * Cria uma exceção para timeout de requisição web assíncrona.
     */
    public static AsyncRequestTimeoutException forWebRequest(String ticker, Duration timeout, String requestId) {
        return new AsyncRequestTimeoutException(ticker, "WEB_REQUEST", timeout, requestId);
    }
    
    /**
     * Cria uma exceção para timeout de processamento de dados assíncrono.
     */
    public static AsyncRequestTimeoutException forDataProcessing(String ticker, Duration timeout) {
        return new AsyncRequestTimeoutException(ticker, "DATA_PROCESSING", timeout);
    }
    
    public Duration getTimeout() {
        return timeout;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * Retorna o tempo de espera recomendado antes de tentar novamente.
     */
    public Duration getRecommendedRetryDelay() {
        // Espera progressiva baseada no timeout original
        return Duration.ofSeconds(Math.min(timeout.getSeconds() / 2, 30));
    }
}