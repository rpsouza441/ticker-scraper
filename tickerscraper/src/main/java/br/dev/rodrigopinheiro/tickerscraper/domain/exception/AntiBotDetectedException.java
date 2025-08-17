package br.dev.rodrigopinheiro.tickerscraper.domain.exception;

/**
 * Exceção lançada quando o sistema anti-bot do site alvo detecta e bloqueia o scraper.
 * Indica necessidade de rotação de user-agents, proxies ou implementação de delays.
 */
public class AntiBotDetectedException extends ScrapingException {
    
    private final String detectionReason;
    private final String detectionMethod;
    
    /**
     * Construtor para detecção de anti-bot.
     * 
     * @param ticker Código do ticker
     * @param url URL onde foi detectado o bloqueio
     * @param detectionReason Razão específica da detecção (CAPTCHA, rate limit, etc.)
     * @param detectionMethod Método que detectou o bloqueio (Playwright, Selenium)
     */
    public AntiBotDetectedException(String ticker, String url, String detectionReason, String detectionMethod) {
        super(String.format("Anti-bot detectado: %s", detectionReason), 
              ticker, url, "ANTI_BOT_DETECTION");
        this.detectionReason = detectionReason;
        this.detectionMethod = detectionMethod;
    }
    
    /**
     * Construtor simplificado.
     */
    public AntiBotDetectedException(String ticker, String url, String detectionReason) {
        this(ticker, url, detectionReason, "UNKNOWN");
    }
    
    public String getDetectionReason() {
        return detectionReason;
    }
    
    public String getDetectionMethod() {
        return detectionMethod;
    }
    
    @Override
    public String getErrorCode() {
        return "ANTI_BOT_DETECTED";
    }
    
    @Override
    public boolean isRetryable() {
        // Anti-bot geralmente requer estratégias específicas antes de retry
        return false;
    }
    
    /**
     * Indica se deve tentar com método alternativo (Selenium se Playwright falhou).
     */
    public boolean shouldTryAlternativeMethod() {
        return "Playwright".equalsIgnoreCase(detectionMethod);
    }
}