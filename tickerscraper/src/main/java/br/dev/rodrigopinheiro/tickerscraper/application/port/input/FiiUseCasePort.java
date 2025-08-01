package br.dev.rodrigopinheiro.tickerscraper.application.port.input;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import reactor.core.publisher.Mono;

public interface FiiUseCasePort {

    Mono<AcaoEntity> getTickerData(String ticker);

    Mono<FiiDadosFinanceirosDTO> getRawTickerData(String ticker);
}
