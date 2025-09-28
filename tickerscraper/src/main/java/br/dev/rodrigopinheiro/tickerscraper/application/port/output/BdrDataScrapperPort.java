package br.dev.rodrigopinheiro.tickerscraper.application.port.output;


import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrDadosFinanceirosDTO;
import reactor.core.publisher.Mono;

public interface BdrDataScrapperPort {
    Mono<BdrDadosFinanceirosDTO> scrape(String ticker);
}