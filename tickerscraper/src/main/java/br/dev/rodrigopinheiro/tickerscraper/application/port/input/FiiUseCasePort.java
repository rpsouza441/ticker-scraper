package br.dev.rodrigopinheiro.tickerscraper.application.port.input;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import reactor.core.publisher.Mono;

public interface FiiUseCasePort {

    Mono<FundoImobiliario> getTickerData(String ticker);

    Mono<FiiDadosFinanceirosDTO> getRawTickerData(String ticker);
}
