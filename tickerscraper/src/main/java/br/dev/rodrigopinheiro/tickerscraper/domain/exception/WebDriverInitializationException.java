package br.dev.rodrigopinheiro.tickerscraper.domain.exception;

/**
 * Exceção lançada quando há falha na inicialização do WebDriver.
 * Indica problemas como ChromeDriver não disponível, versão incompatível,
 * problemas de permissão ou configuração incorreta.
 */
public class WebDriverInitializationException extends ScrapingException {
    
    private final String driverType;
    
    /**
     * Construtor para falha de inicialização do WebDriver.
     * 
     * @param message Mensagem descritiva do erro
     * @param driverType Tipo do driver (Chrome, Firefox, etc.)
     * @param cause Causa raiz da exceção
     */
    public WebDriverInitializationException(String message, String driverType, Throwable cause) {
        super(message, null, null, "WEBDRIVER_INIT", cause);
        this.driverType = driverType;
    }
    
    /**
     * Construtor simplificado para ChromeDriver.
     */
    public WebDriverInitializationException(String message, Throwable cause) {
        this(message, "ChromeDriver", cause);
    }
    
    public String getDriverType() {
        return driverType;
    }
    
    @Override
    public String getErrorCode() {
        return "WEBDRIVER_INITIALIZATION_FAILED";
    }
    
    @Override
    public boolean isRetryable() {
        return true; // Falhas de inicialização podem ser tentadas novamente
    }
}