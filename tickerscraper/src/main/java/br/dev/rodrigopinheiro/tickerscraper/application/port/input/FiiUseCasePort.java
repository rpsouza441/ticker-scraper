package br.dev.rodrigopinheiro.tickerscraper.application.port.input;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.FiiRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;
import reactor.core.publisher.Mono;

/**
 * Porta de entrada para casos de uso relacionados a FIIs.
 * Define as operações disponíveis mantendo independência da infraestrutura.
 */
public interface FiiUseCasePort {

    /**
     * Obtém dados processados de um FII.
     * 
     * @param ticker Código do FII
     * @return Entidade de domínio FundoImobiliario com dados estruturados
     */
    Mono<FundoImobiliario> getTickerData(String ticker);

    /**
     * Obtém dados brutos de um FII para análise ou debugging.
     * 
     * @param ticker Código do FII
     * @return DTO da application com dados não processados
     */
    Mono<FiiRawDataResponse> getRawTickerData(String ticker);
}
