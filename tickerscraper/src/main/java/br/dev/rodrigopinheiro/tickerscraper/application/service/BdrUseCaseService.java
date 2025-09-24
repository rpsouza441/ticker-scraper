package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.BdrRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.mapper.RawDataMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.BdrUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.BdrDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.BdrRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.service.base.AbstractTickerUseCaseService;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.Bdr;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.mapper.BdrScraperMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class BdrUseCaseService extends AbstractTickerUseCaseService<BdrDadosFinanceirosDTO, Bdr, BdrRawDataResponse>
        implements BdrUseCasePort {

    private final BdrDataScrapperPort scraper;
    private final BdrRepositoryPort repository;
    private final BdrScraperMapper scraperMapper;
    private final RawDataMapper rawDataMapper;

    public BdrUseCaseService(@Qualifier("bdrPlaywrightScraper") BdrDataScrapperPort scraper,
                             BdrRepositoryPort repository,
                             BdrScraperMapper scraperMapper,
                             RawDataMapper rawDataMapper,
                             ObjectMapper objectMapper) {
        super(objectMapper, Duration.ofHours(12), BdrDadosFinanceirosDTO.class);
        this.scraper = scraper;
        this.repository = repository;
        this.scraperMapper = scraperMapper;
        this.rawDataMapper = rawDataMapper;
    }

    @Override
    protected String normalize(String ticker) {
        return ticker == null ? null : ticker.trim().toUpperCase();
    }

    @Override
    protected Mono<Optional<Bdr>> findByTicker(String ticker) {
        return Mono.fromCallable(() -> repository.findByTickerWithDetails(ticker))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    protected Mono<BdrDadosFinanceirosDTO> scrape(String ticker) {
        return scraper.scrape(ticker);
    }

    @Override
    protected boolean isCacheValid(Bdr domain, Duration maxAge) {
        Instant updatedAt = domain.getUpdatedAt();
        return updatedAt != null && Duration.between(updatedAt, Instant.now()).compareTo(maxAge) < 0;
    }

    @Override
    protected Bdr toDomain(BdrDadosFinanceirosDTO raw) {
        return scraperMapper.toDomain(raw);
    }

    @Override
    protected Bdr saveDomain(Bdr domain, BdrDadosFinanceirosDTO raw) {
        return repository.saveReplacingChildren(domain);
    }

    @Override
    public Mono<BdrRawDataResponse> getRawTickerData(String ticker) {
        return super.getRawTickerDataAsResponse(ticker);
    }

    @Override
    protected BdrRawDataResponse convertToRawResponse(BdrDadosFinanceirosDTO infraDto) {
        return rawDataMapper.toBdrRawDataResponse(infraDto);
    }

    @Override
    protected BdrRawDataResponse createFailedResponse(String ticker, String source, String error) {
        return rawDataMapper.createFailedBdrResponse(ticker, source, error);
    }

}
