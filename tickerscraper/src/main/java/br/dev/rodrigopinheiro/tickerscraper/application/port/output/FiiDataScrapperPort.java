package br.dev.rodrigopinheiro.tickerscraper.application.port.output;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import reactor.core.publisher.Mono;


public interface FiiDataScrapperPort {
    Mono<FiiDadosFinanceirosDTO> scrape(String ticker);

}
