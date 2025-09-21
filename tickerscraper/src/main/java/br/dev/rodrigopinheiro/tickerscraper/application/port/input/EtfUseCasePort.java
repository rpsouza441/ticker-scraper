package br.dev.rodrigopinheiro.tickerscraper.application.port.input;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.EtfRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Etf;
import reactor.core.publisher.Mono;

/**
 * Porta de entrada para casos de uso relacionados a ETFs.
 * Define as operações disponíveis mantendo independência da infraestrutura.
 */
public interface EtfUseCasePort {

    /**
     * Obtém dados processados de um ETF.
     * 
     * @param ticker Código do ETF
     * @return Entidade de domínio Etf com dados estruturados
     */
    Mono<Etf> getTickerData(String ticker);

    /**
     * Obtém dados brutos de um ETF para análise ou debugging.
     * 
     * @param ticker Código do ETF
     * @return DTO da application com dados não processados
     */
    Mono<EtfRawDataResponse> getRawTickerData(String ticker);
}