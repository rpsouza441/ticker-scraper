package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.BdrRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.mapper.RawDataMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.BdrUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.BdrDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.BdrRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.service.base.AbstractTickerUseCaseService;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.mapper.BdrScraperMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
public class BdrUseCaseService
        extends AbstractTickerUseCaseService<BdrDadosFinanceirosDTO, Bdr, BdrRawDataResponse>
        implements BdrUseCasePort {

    private final BdrDataScrapperPort scraper;
    private final BdrRepositoryPort repo;
    private final BdrScraperMapper scraperMapper;
    private final RawDataMapper rawDataMapper;

    public BdrUseCaseService(@Qualifier("bdrPlaywrightDirectScraper") BdrDataScrapperPort scraper,
                             BdrRepositoryPort repo,
                             BdrScraperMapper scraperMapper,
                             RawDataMapper rawDataMapper,
                             ObjectMapper objectMapper) {
        super(objectMapper, Duration.ofDays(1), BdrDadosFinanceirosDTO.class);
        this.scraper = scraper;
        this.repo = repo;
        this.scraperMapper = scraperMapper;
        this.rawDataMapper = rawDataMapper;
    }

    @Override protected String normalize(String t) { return t == null ? null : t.trim().toUpperCase(); }

    @Override
    @Transactional(readOnly = true)
    protected Mono<Optional<Bdr>> findByTicker(String t) {
        final String key = normalize(t);
        return Mono.fromCallable(() -> repo.findByTickerWithDividendos(key))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override protected Mono<BdrDadosFinanceirosDTO> scrape(String t) { return scraper.scrape(t); }

    @Override
    protected boolean isCacheValid(Bdr d, Duration maxAge) {
        var ts = d.getDataAtualizacao();
        return ts != null && Duration.between(ts, Instant.now()).compareTo(maxAge) < 0;
    }


    @Override protected Bdr toDomain(BdrDadosFinanceirosDTO raw) { return scraperMapper.toDomain(raw); }

    @Override
    protected Bdr saveDomain(Bdr domain, BdrDadosFinanceirosDTO raw) {
        return repo.saveReplacingDividends(domain);
    }

    @Override
    protected Mono<BdrDadosFinanceirosDTO> readRawFromStore(String ticker) { return Mono.empty(); }

    @Override public Mono<BdrRawDataResponse> getRawTickerData(String ticker) {
        return super.getRawTickerDataAsResponse(ticker);
    }

    @Override
    protected BdrRawDataResponse convertToRawResponse(BdrDadosFinanceirosDTO infraDto) {
        return rawDataMapper.toBdrRawDataResponse(infraDto, Map.of());
    }

    @Override
    protected BdrRawDataResponse createFailedResponse(String ticker, String source, String error) {
        return rawDataMapper.createFailedBdrResponse(ticker, source, error);
    }
}
