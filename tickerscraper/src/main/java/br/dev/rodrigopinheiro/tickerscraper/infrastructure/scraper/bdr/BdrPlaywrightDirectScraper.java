package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.BdrDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrDadosFinanceirosDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Qualifier("bdrPlaywrightDirectScraper")
public class BdrPlaywrightDirectScraper implements BdrDataScrapperPort {

    @Override
    public Mono<BdrDadosFinanceirosDTO> scrape(String ticker) {
        // STUB: retorna DTO vazio para permitir iterações futuras sem quebrar.
        return Mono.just(new BdrDadosFinanceirosDTO(
                null, null, null, null, null, null
        ));
    }
}
