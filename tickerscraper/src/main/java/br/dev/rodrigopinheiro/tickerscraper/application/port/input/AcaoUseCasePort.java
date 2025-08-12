package br.dev.rodrigopinheiro.tickerscraper.application.port.input;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoDadosFinanceirosDTO;
import reactor.core.publisher.Mono;

public interface AcaoUseCasePort {

    Mono<Acao> getTickerData(String ticker);

    Mono<AcaoDadosFinanceirosDTO> getRawTickerData(String ticker);
}
