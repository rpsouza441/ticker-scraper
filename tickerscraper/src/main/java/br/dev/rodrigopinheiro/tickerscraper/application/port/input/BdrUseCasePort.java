package br.dev.rodrigopinheiro.tickerscraper.application.port.input;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.BdrRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;
import reactor.core.publisher.Mono;

/**
 * Porta de entrada para casos de uso relacionados a BDRs.
 * Define as operações disponíveis mantendo independência da infraestrutura.
 */
public interface BdrUseCasePort {

    /**
     * Obtém dados processados de um BDR.
     *
     * @param ticker Código do BDR (ex.: AAPL34)
     * @return Entidade de domínio Bdr com dados estruturados
     */
    Mono<Bdr> getTickerData(String ticker);

    /**
     * Obtém dados brutos de um BDR para análise ou debugging.
     *
     * @param ticker Código do BDR
     * @return DTO da application com dados não processados
     */
    Mono<BdrRawDataResponse> getRawTickerData(String ticker);
}
