package br.dev.rodrigopinheiro.tickerscraper.application.service;


import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.FiiPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.FiiUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.service.base.AbstractTickerUseCaseService;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.mapper.FiiScraperMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FiiUseCaseService
        extends AbstractTickerUseCaseService<FiiDadosFinanceirosDTO, FundoImobiliario>
        implements FiiUseCasePort {

    private final FiiDataScrapperPort scraper;
    private final FiiRepositoryPort repo;
    private final FiiScraperMapper scraperMapper;

    public FiiUseCaseService(@Qualifier("fiiPlaywrightDirectScraper") FiiDataScrapperPort scraper,
                             FiiRepositoryPort repo,
                             FiiScraperMapper scraperMapper,
                             ObjectMapper objectMapper) {
        super(objectMapper, Duration.ofDays(1), FiiDadosFinanceirosDTO.class);
        this.scraper = scraper;
        this.repo = repo;
        this.scraperMapper = scraperMapper;
    }
    @Override protected String normalize(String t) { return t == null ? null : t.trim().toUpperCase(); }

    @Override protected Mono<Optional<FundoImobiliario>> findByTicker(String t) {
        // USE o método com dividendos (fetch join no adapter JPA)
        return Mono.fromCallable(() -> repo.findByTickerWithDividendos(t))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override protected Mono<FiiDadosFinanceirosDTO> scrape(String t) { return scraper.scrape(t); }

    @Override protected boolean isCacheValid(FundoImobiliario d, Duration maxAge) {
        return d.getDataAtualizacao() != null &&
                java.time.Duration.between(d.getDataAtualizacao(), java.time.LocalDateTime.now())
                        .compareTo(maxAge) < 0;
    }

    @Override protected FundoImobiliario toDomain(FiiDadosFinanceirosDTO raw) {
        return scraperMapper.toDomain(raw);
    }

    @Override
    protected FundoImobiliario saveDomain(FundoImobiliario domain, FiiDadosFinanceirosDTO raw) {
        if (raw.internalId() == null) {
            throw new IllegalStateException("internalId ausente no RAW para " + domain.getTicker());
        }
        String audit = null;
        try { audit = serialize(raw); } catch (Exception ignored) {}
        return repo.saveReplacingDividends(domain, raw.internalId().longValue(), audit);
    }

    @Override protected Mono<FiiDadosFinanceirosDTO> readRawFromStore(String ticker) {
        // precisa de um método no port para pegar o JSON cru
        return Mono.fromCallable(() -> repo.findRawJsonByTicker(ticker))
                .flatMap(opt -> opt.map(json -> {
                    try { return Mono.just(deserialize(json)); }
                    catch (Exception e) { return Mono.<FiiDadosFinanceirosDTO>error(e); }
                }).orElseGet(Mono::empty))
                .subscribeOn(Schedulers.boundedElastic());
    }
}