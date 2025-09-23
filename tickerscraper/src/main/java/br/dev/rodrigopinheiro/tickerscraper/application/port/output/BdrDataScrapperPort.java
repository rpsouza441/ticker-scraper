package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrDadosFinanceirosDTO;
import reactor.core.publisher.Mono;

/**
 * Porta de saída responsável por realizar o scraping de dados financeiros de BDRs.
 * Fornece uma API reativa para recuperar o pacote consolidado de informações capturadas
 * na camada de infraestrutura.
 */
public interface BdrDataScrapperPort {

    /**
     * Executa o processo de scraping para o ticker informado.
     *
     * @param ticker código do BDR que será processado
     * @return Mono contendo o agregado de dados financeiros coletados
     */
    Mono<BdrDadosFinanceirosDTO> scrape(String ticker);
}
