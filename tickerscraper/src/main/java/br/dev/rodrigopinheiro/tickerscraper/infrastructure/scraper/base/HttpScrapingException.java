package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base;

import br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingException;

/**
 * Exceção específica para erros HTTP durante o scraping.
 */
public class HttpScrapingException extends ScrapingException {
    
    private final int httpStatus;
    
    public HttpScrapingException(int httpStatus, String statusText, String ticker, String url) {
        super("Erro HTTP " + httpStatus + ": " + statusText, ticker, url, "HTTP_ERROR");
        this.httpStatus = httpStatus;
    }
    
    @Override
    public String getErrorCode() {
        return "HTTP_" + httpStatus;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
}