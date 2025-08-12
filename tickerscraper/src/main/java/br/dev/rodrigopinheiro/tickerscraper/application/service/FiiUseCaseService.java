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
        extends AbstractTickerUseCaseService<FiiDadosFinanceirosDTO, FundoImobiliario, FundoImobiliarioEntity>
        implements FiiUseCasePort {

    private final FiiDataScrapperPort scraper;
    private final FiiRepositoryPort repo;
    private final FiiScraperMapper scraperMapper;
    private final FiiPersistenceMapper persistenceMapper;

    public FiiUseCaseService(@Qualifier("fiiPlaywrightDirectScraper") FiiDataScrapperPort scraper,
                             FiiRepositoryPort repo,
                             FiiScraperMapper scraperMapper,
                             FiiPersistenceMapper persistenceMapper,
                             ObjectMapper objectMapper) {
        super(objectMapper, Duration.ofDays(1), FiiDadosFinanceirosDTO.class);
        this.scraper = scraper;
        this.repo = repo;
        this.scraperMapper = scraperMapper;
        this.persistenceMapper = persistenceMapper;
    }

    @Override
    protected String normalize(String ticker) {
        return ticker == null ? null : ticker.trim().toUpperCase();
    }

    @Override
    protected Mono<Optional<FundoImobiliarioEntity>> findByTicker(String t) {
        return Mono.fromCallable(() -> repo.findByTicker(t)).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    protected Mono<FiiDadosFinanceirosDTO> scrape(String t) {
        return scraper.scrape(t);
    }

    @Override
    protected boolean isCacheValid(FundoImobiliarioEntity e, Duration maxAge) {
        return Duration.between(e.getDataAtualizacao(), LocalDateTime.now()).compareTo(maxAge) < 0;
    }

    @Override
    protected FundoImobiliario toDomain(FiiDadosFinanceirosDTO raw) {
        return scraperMapper.toDomain(raw);
    }

    @Override
    protected FundoImobiliarioEntity toEntity(FundoImobiliario domain) {
        return persistenceMapper.toEntity(domain);
    }

    @Override
    protected void enrichEntity(FundoImobiliarioEntity entity, FiiDadosFinanceirosDTO raw) {
        entity.setInternalId(raw.internalId().longValue());
        serializeRawInto(entity, raw);
    }

    @Override
    protected void mergeForUpdate(FundoImobiliarioEntity existente, FundoImobiliarioEntity mapped) {
        mapped.setId(existente.getId());
    }

    @Override
    protected FundoImobiliarioEntity save(FundoImobiliarioEntity e) {
        return repo.save(e);
    }

    @Override
    protected LocalDateTime entityUpdatedAt(FundoImobiliarioEntity e) {
        return e.getDataAtualizacao();
    }

    @Override
    protected String entityRawJson(FundoImobiliarioEntity e) {
        return e.getDadosBrutosJson();
    }

    @Override
    protected void setEntityRawJson(FundoImobiliarioEntity e, String json) {
        e.setDadosBrutosJson(json);
    }
}