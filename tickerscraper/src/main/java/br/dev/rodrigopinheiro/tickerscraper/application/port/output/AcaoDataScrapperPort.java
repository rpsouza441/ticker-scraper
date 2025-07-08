package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoDadosFinanceiros;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface AcaoDataScrapperPort {
    Mono<AcaoDadosFinanceiros> scrape(String ticker) throws IOException;

}
