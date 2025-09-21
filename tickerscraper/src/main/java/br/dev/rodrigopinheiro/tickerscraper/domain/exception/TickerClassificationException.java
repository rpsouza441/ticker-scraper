package br.dev.rodrigopinheiro.tickerscraper.domain.exception;

public class TickerClassificationException extends RuntimeException {
    public TickerClassificationException(String ticker, String motivo) {
        super(String.format("Erro ao classificar ticker '%s': %s", ticker, motivo));
    }
    
    public TickerClassificationException(String ticker, Throwable cause) {
        super(String.format("Erro ao classificar ticker '%s'", ticker), cause);
    }
}