package br.dev.rodrigopinheiro.tickerscraper.domain.exception;

import java.util.List;

/**
 * Exceção lançada quando o ticker solicitado não é encontrado no site.
 * Pode incluir sugestões de tickers similares se disponíveis.
 */
public class TickerNotFoundException extends ScrapingException {
    
    private final List<String> similarTickers;
    private final String searchAttempted;
    
    /**
     * Construtor para ticker não encontrado.
     * 
     * @param ticker Código do ticker procurado
     * @param url URL onde foi feita a busca
     * @param searchAttempted Termo de busca utilizado
     * @param similarTickers Lista de tickers similares encontrados (opcional)
     */
    public TickerNotFoundException(String ticker, String url, String searchAttempted, List<String> similarTickers) {
        super(buildMessage(ticker, searchAttempted, similarTickers), 
              ticker, url, "TICKER_SEARCH");
        this.searchAttempted = searchAttempted;
        this.similarTickers = similarTickers != null ? List.copyOf(similarTickers) : List.of();
    }
    
    /**
     * Construtor simplificado.
     */
    public TickerNotFoundException(String ticker, String url) {
        this(ticker, url, ticker, null);
    }
    
    private static String buildMessage(String ticker, String searchAttempted, List<String> similarTickers) {
        StringBuilder message = new StringBuilder(String.format("Ticker '%s' não encontrado", ticker));
        
        if (searchAttempted != null && !searchAttempted.equals(ticker)) {
            message.append(String.format(" (busca: '%s')", searchAttempted));
        }
        
        if (similarTickers != null && !similarTickers.isEmpty()) {
            message.append(String.format(". Sugestões: %s", String.join(", ", similarTickers)));
        }
        
        return message.toString();
    }
    
    public List<String> getSimilarTickers() {
        return similarTickers;
    }
    
    public String getSearchAttempted() {
        return searchAttempted;
    }
    
    /**
     * Indica se existem sugestões de tickers similares.
     */
    public boolean hasSuggestions() {
        return !similarTickers.isEmpty();
    }
    
    @Override
    public String getErrorCode() {
        return "TICKER_NOT_FOUND";
    }
    
    @Override
    public boolean isRetryable() {
        return false; // Ticker não encontrado não deve ser tentado novamente
    }
}