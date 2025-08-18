package br.dev.rodrigopinheiro.tickerscraper.domain.exception;

/**
 * Exceção lançada quando há problemas na estrutura HTML da página.
 * Indica HTML malformado, estrutura alterada, elementos ausentes
 * ou mudanças no layout do site.
 */
public class HtmlStructureException extends ScrapingException {
    
    private final String expectedElement;
    private final String actualContent;
    
    /**
     * Construtor para problemas de estrutura HTML.
     * 
     * @param ticker Código do ticker
     * @param url URL da página
     * @param message Mensagem descritiva do erro
     * @param expectedElement Elemento HTML esperado
     * @param actualContent Conteúdo atual encontrado
     * @param cause Causa raiz da exceção
     */
    public HtmlStructureException(String ticker, String url, String message, 
                                 String expectedElement, String actualContent, Throwable cause) {
        super(message, ticker, url, "HTML_STRUCTURE", cause);
        this.expectedElement = expectedElement;
        this.actualContent = actualContent;
    }
    
    /**
     * Construtor simplificado.
     */
    public HtmlStructureException(String ticker, String url, String message, Throwable cause) {
        this(ticker, url, message, null, null, cause);
    }
    
    /**
     * Construtor para estrutura inválida sem causa.
     */
    public HtmlStructureException(String ticker, String url, String message) {
        this(ticker, url, message, null, null, null);
    }
    
    /**
     * Factory method para elemento específico não encontrado.
     */
    public static HtmlStructureException forMissingElement(String ticker, String url, String expectedElement) {
        return new HtmlStructureException(ticker, url, "Elemento HTML esperado não encontrado: " + expectedElement, 
             expectedElement, null, null);
    }
    
    public String getExpectedElement() {
        return expectedElement;
    }
    
    public String getActualContent() {
        return actualContent;
    }
    
    @Override
    public String getErrorCode() {
        return "HTML_STRUCTURE_INVALID";
    }
    
    @Override
    public boolean isRetryable() {
        return false; // Problemas de estrutura HTML geralmente não são resolvidos com retry
    }
}