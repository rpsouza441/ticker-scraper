package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoDadosFinanceirosDTO;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface AcaoDataScrapperPort {
    Mono<AcaoDadosFinanceirosDTO> scrape(String ticker) throws IOException;

}
