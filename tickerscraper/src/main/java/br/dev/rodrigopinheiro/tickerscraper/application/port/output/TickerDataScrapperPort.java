package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.*;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface TickerDataScrapperPort {
    Mono<DadosFinanceiros> scrape(String ticker) throws IOException;

}
