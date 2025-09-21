package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.dto.EtfDadosFinanceirosDTO;
import reactor.core.publisher.Mono;

/**
 * Porta de saída para scraping de dados de ETFs.
 * Define o contrato para obtenção de dados de ETFs mantendo independência da infraestrutura.
 */
public interface EtfScraperPort {

    /**
     * Realiza scraping dos dados financeiros de um ETF.
     * 
     * @param ticker Código do ETF
     * @return DTO com dados financeiros do ETF
     */
    Mono<EtfDadosFinanceirosDTO> scrapeEtfData(String ticker);
}