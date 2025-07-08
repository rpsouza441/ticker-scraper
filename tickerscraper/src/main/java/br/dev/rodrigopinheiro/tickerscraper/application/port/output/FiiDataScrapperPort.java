package br.dev.rodrigopinheiro.tickerscraper.application.port.output;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceiros;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface FiiDataScrapperPort {
    Mono<FiiDadosFinanceiros> scrape(String ticker) throws IOException;

}
