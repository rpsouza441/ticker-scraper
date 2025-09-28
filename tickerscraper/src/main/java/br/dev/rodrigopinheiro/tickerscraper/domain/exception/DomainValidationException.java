package br.dev.rodrigopinheiro.tickerscraper.domain.exception;


import br.dev.rodrigopinheiro.tickerscraper.domain.exception.enums.DomainErrorCode;

public final class DomainValidationException extends DomainException {
    public DomainValidationException(String message) {
        super(DomainErrorCode.DOMAIN_VALIDATION, message);
    }
}