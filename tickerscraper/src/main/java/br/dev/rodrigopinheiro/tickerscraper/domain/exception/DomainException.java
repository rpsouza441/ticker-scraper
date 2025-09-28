package br.dev.rodrigopinheiro.tickerscraper.domain.exception;


import br.dev.rodrigopinheiro.tickerscraper.domain.exception.enums.DomainErrorCode;

public abstract class DomainException extends RuntimeException {
  private final DomainErrorCode code;

  protected DomainException(DomainErrorCode code, String message) {
    super(message);
    this.code = code;
  }
  protected DomainException(DomainErrorCode code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public DomainErrorCode getCode() { return code; }
}