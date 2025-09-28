package br.dev.rodrigopinheiro.tickerscraper.domain.exception;


import br.dev.rodrigopinheiro.tickerscraper.domain.exception.enums.DomainErrorCode;

public final class DividendInvalidException extends DomainException {
    public DividendInvalidException(String message) {
        super(DomainErrorCode.DIVIDEND_INVALID, message);
    }
}