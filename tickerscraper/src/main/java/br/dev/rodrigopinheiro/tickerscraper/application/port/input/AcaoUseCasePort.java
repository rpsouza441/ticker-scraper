package br.dev.rodrigopinheiro.tickerscraper.application.port.input;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoDadosFinanceirosDTO;
import reactor.core.publisher.Mono;

public interface AcaoUseCasePort {

    Mono<AcaoEntity> getTickerData(String ticker);

    Mono<AcaoDadosFinanceirosDTO> getRawTickerData(String ticker);
}
