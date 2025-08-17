package br.dev.rodrigopinheiro.tickerscraper.domain.exception;

/**
 * Exceção lançada quando há falha no parsing/extração de dados do HTML.
 * Indica mudanças na estrutura do site ou seletores CSS/XPath inválidos.
 */
public class DataParsingException extends ScrapingException {
    
    private final String selector;
    private final String expectedData;
    private final String actualContent;
    
    /**
     * Construtor para erros de parsing.
     * 
     * @param ticker Código do ticker
     * @param url URL onde ocorreu o erro de parsing
     * @param selector Seletor CSS/XPath que falhou
     * @param expectedData Tipo de dado esperado
     * @param actualContent Conteúdo encontrado (limitado)
     * @param cause Exceção original (NumberFormatException, etc.)
     */
    public DataParsingException(String ticker, String url, String selector, 
                               String expectedData, String actualContent, Throwable cause) {
        super(String.format("Falha ao parsear '%s' com seletor '%s'. Esperado: %s, Encontrado: %s", 
                           expectedData, selector, expectedData, 
                           actualContent != null ? actualContent.substring(0, Math.min(50, actualContent.length())) : "null"), 
              ticker, url, "DATA_PARSING", cause);
        this.selector = selector;
        this.expectedData = expectedData;
        this.actualContent = actualContent;
    }
    
    /**
     * Construtor simplificado para seletor não encontrado.
     */
    public DataParsingException(String ticker, String url, String selector, String expectedData) {
        this(ticker, url, selector, expectedData, null, null);
    }
    
    /**
     * Construtor para erro de conversão de tipo.
     */
    public static DataParsingException forTypeConversion(String ticker, String url, 
                                                        String selector, String expectedType, 
                                                        String actualValue, Throwable cause) {
        return new DataParsingException(ticker, url, selector, expectedType, actualValue, cause);
    }
    
    /**
     * Construtor para elemento não encontrado.
     */
    public static DataParsingException forMissingElement(String ticker, String url, String selector) {
        return new DataParsingException(ticker, url, selector, "elemento HTML", "não encontrado", null);
    }
    
    public String getSelector() {
        return selector;
    }
    
    public String getExpectedData() {
        return expectedData;
    }
    
    public String getActualContent() {
        return actualContent;
    }
    
    @Override
    public String getErrorCode() {
        return "DATA_PARSING_ERROR";
    }
    
    @Override
    public boolean isRetryable() {
        // Erros de parsing geralmente indicam mudança estrutural, não vale retry imediato
        return false;
    }
}