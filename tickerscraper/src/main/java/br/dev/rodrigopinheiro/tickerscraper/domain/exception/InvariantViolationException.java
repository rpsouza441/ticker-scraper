package br.dev.rodrigopinheiro.tickerscraper.domain.exception;


import br.dev.rodrigopinheiro.tickerscraper.domain.exception.enums.DomainErrorCode;

public final class InvariantViolationException extends DomainException {
    public InvariantViolationException(String message) {
        super(DomainErrorCode.INVARIANT_VIOLATION, message);
    }
}