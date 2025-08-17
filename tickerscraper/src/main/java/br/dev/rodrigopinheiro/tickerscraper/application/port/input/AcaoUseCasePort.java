package br.dev.rodrigopinheiro.tickerscraper.application.port.input;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.AcaoRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import reactor.core.publisher.Mono;

/**
 * Porta de entrada para casos de uso relacionados a ações.
 * Define as operações disponíveis mantendo independência da infraestrutura.
 */
public interface AcaoUseCasePort {

    /**
     * Obtém dados processados de uma ação.
     * 
     * @param ticker Código da ação
     * @return Entidade de domínio Acao com dados estruturados
     */
    Mono<Acao> getTickerData(String ticker);

    /**
     * Obtém dados brutos de uma ação para análise ou debugging.
     * 
     * @param ticker Código da ação
     * @return DTO da application com dados não processados
     */
    Mono<AcaoRawDataResponse> getRawTickerData(String ticker);
}
