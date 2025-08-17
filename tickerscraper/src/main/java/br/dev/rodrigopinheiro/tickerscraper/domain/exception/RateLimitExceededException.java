package br.dev.rodrigopinheiro.tickerscraper.domain.exception;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Exceção lançada quando o rate limit é excedido.
 * Indica necessidade de implementar backoff ou aguardar período de cooldown.
 */
public class RateLimitExceededException extends ScrapingException {
    
    private final int requestCount;
    private final Duration timeWindow;
    private final LocalDateTime retryAfter;
    
    /**
     * Construtor para rate limit excedido.
     * 
     * @param ticker Código do ticker
     * @param url URL afetada
     * @param requestCount Número de requisições feitas
     * @param timeWindow Janela de tempo do rate limit
     * @param retryAfter Momento em que pode tentar novamente
     */
    public RateLimitExceededException(String ticker, String url, int requestCount, 
                                     Duration timeWindow, LocalDateTime retryAfter) {
        super(String.format("Rate limit excedido: %d requisições em %d segundos. Retry após: %s", 
                           requestCount, timeWindow.getSeconds(), retryAfter), 
              ticker, url, "RATE_LIMITING");
        this.requestCount = requestCount;
        this.timeWindow = timeWindow;
        this.retryAfter = retryAfter;
    }
    
    /**
     * Construtor simplificado.
     */
    public RateLimitExceededException(String ticker, String url) {
        this(ticker, url, 0, Duration.ZERO, LocalDateTime.now().plusMinutes(5));
    }
    
    public int getRequestCount() {
        return requestCount;
    }
    
    public Duration getTimeWindow() {
        return timeWindow;
    }
    
    public LocalDateTime getRetryAfter() {
        return retryAfter;
    }
    
    /**
     * Calcula o tempo de espera necessário antes do próximo retry.
     */
    public Duration getWaitTime() {
        return Duration.between(LocalDateTime.now(), retryAfter);
    }
    
    @Override
    public String getErrorCode() {
        return "RATE_LIMIT_EXCEEDED";
    }
    
    @Override
    public boolean isRetryable() {
        return true; // Pode ser tentado após o período de cooldown
    }
}