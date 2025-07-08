package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.FiiUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceiros;
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
    @Override
    public Mono<FiiDadosFinanceiros> getRawTickerData(String ticker) {
        try {
            // Simplesmente repassa a chamada para o scraper
            return fiiDataScrapperPort.scrape(ticker);
        } catch (IOException e) {
            return Mono.error(new RuntimeException(e));
        }
    }

    @Override
    public Mono<AcaoEntity> getTickerData(String ticker) {
        return Mono.empty();
    }
}
