package br.dev.rodrigopinheiro.tickerscraper.domain.exception;

/**
 * Exceção lançada quando há falha na captura de requisições de rede via DevTools.
 * Indica problemas como DevTools não disponível, listener falhou,
 * ou APIs esperadas não foram capturadas.
 */
public class NetworkCaptureException extends ScrapingException {
    
    private final int capturedUrls;
    private final int expectedUrls;
    
    /**
     * Construtor para falha de captura de rede.
     * 
     * @param ticker Código do ticker
     * @param message Mensagem descritiva do erro
     * @param capturedUrls Número de URLs capturadas
     * @param expectedUrls Número de URLs esperadas
     * @param cause Causa raiz da exceção
     */
    public NetworkCaptureException(String ticker, String message, int capturedUrls, int expectedUrls, Throwable cause) {
        super(message, ticker, null, "NETWORK_CAPTURE", cause);
        this.capturedUrls = capturedUrls;
        this.expectedUrls = expectedUrls;
    }
    
    /**
     * Construtor simplificado.
     */
    public NetworkCaptureException(String ticker, String message, Throwable cause) {
        this(ticker, message, 0, 3, cause); // Esperamos 3 APIs por padrão (cotação, dividendos, histórico)
    }
    
    /**
     * Construtor para timeout de captura.
     */
    public NetworkCaptureException(String ticker, String message) {
        this(ticker, message, null);
    }
    
    public int getCapturedUrls() {
        return capturedUrls;
    }
    
    public int getExpectedUrls() {
        return expectedUrls;
    }
    
    @Override
    public String getErrorCode() {
        return "NETWORK_CAPTURE_FAILED";
    }
    
    @Override
    public boolean isRetryable() {
        return true; // Problemas de captura de rede podem ser tentados novamente
    }
}