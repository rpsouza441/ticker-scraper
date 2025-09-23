package br.dev.rodrigopinheiro.tickerscraper.application.port.input;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.BdrRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.Bdr;
import reactor.core.publisher.Mono;

/**
 * Porta de entrada para operações relacionadas a BDRs.
 *
 * <p>Expõe contratos reativos que isolam a camada de aplicação dos
 * detalhes de infraestrutura (scrapers, repositórios, etc.).</p>
 */
public interface BdrUseCasePort {

    /**
     * Obtém os dados já processados de um BDR a partir do ticker informado.
     *
     * @param ticker código do ativo
     * @return representação de domínio com os dados consolidados
     */
    Mono<Bdr> getTickerData(String ticker);

    /**
     * Obtém os dados brutos coletados no scraping para inspeção/debug.
     *
     * @param ticker código do ativo
     * @return DTO com os dados crus e metadados da coleta
     */
    Mono<BdrRawDataResponse> getRawTickerData(String ticker);
}
