package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.FiiUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class FiiUserCaseService implements FiiUseCasePort {
    private static final Logger logger = LoggerFactory.getLogger(FiiUserCaseService.class);

    private final FiiDataScrapperPort fiiDataScrapperPort;

    public FiiUserCaseService(@Qualifier("fiiSeleniumScraper") FiiDataScrapperPort fiiDataScrapperPort) {
        this.fiiDataScrapperPort = fiiDataScrapperPort;
    }

    // Este é o métod que seu controller /raw vai chamar.
    //TODO verifica se existe. Se nao chama o descobridor. Se existe e esta desatualiado chama o atualizador
    @Override
    public Mono<FiiDadosFinanceirosDTO> getRawTickerData(String ticker) {

        // Simplesmente repassa a chamada para o scraper
        return fiiDataScrapperPort.scrape(ticker);

    }

    @Override
    public Mono<AcaoEntity> getTickerData(String ticker) {
        return Mono.empty();
    }
}
