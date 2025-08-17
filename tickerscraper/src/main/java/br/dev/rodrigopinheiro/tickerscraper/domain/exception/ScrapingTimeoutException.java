package br.dev.rodrigopinheiro.tickerscraper.domain.exception;

import java.time.Duration;

/**
 * Exceção lançada quando uma operação de scraping excede o tempo limite configurado.
 * Indica problemas de conectividade ou lentidão do site alvo.
 */
public class ScrapingTimeoutException extends ScrapingException {
    
    private final Duration timeout;
    
    /**
     * Construtor para timeout de scraping.
     * 
     * @param ticker Código do ticker
     * @param url URL que causou timeout
     * @param timeout Tempo limite que foi excedido
     * @param operation Operação específica (navegação, seletor, etc.)
     */
    public ScrapingTimeoutException(String ticker, String url, Duration timeout, String operation) {
        super(String.format("Timeout após %d segundos", timeout.getSeconds()), 
              ticker, url, operation);
        this.timeout = timeout;
    }
    
    /**
     * Construtor simplificado para timeout genérico.
     */
    public ScrapingTimeoutException(String ticker, String url, Duration timeout) {
        this(ticker, url, timeout, "SCRAPING");
    }
    
    public Duration getTimeout() {
        return timeout;
    }
    
    @Override
    public String getErrorCode() {
        return "SCRAPING_TIMEOUT";
    }
    
    @Override
    public boolean isRetryable() {
        return true; // Timeouts podem ser tentados novamente
    }
}