package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.AcaoPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.AcaoUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.application.service.base.AbstractTickerUseCaseService;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.mapper.AcaoScraperMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class AcaoUseCaseService extends AbstractTickerUseCaseService<AcaoDadosFinanceirosDTO, Acao, AcaoEntity> implements AcaoUseCasePort {
    private static final Logger logger = LoggerFactory.getLogger(AcaoUseCaseService.class);
    private final AcaoDataScrapperPort scraper;
    private final AcaoRepositoryPort repo;
    private final AcaoScraperMapper scraperMapper;
    private final AcaoPersistenceMapper persistenceMapper;

    public AcaoUseCaseService(@Qualifier("acaoPlaywrightScraper") AcaoDataScrapperPort scraper,
                              AcaoRepositoryPort repo,
                              AcaoScraperMapper scraperMapper,
                              AcaoPersistenceMapper persistenceMapper,
                              ObjectMapper objectMapper) {
        super(objectMapper, Duration.ofDays(1), AcaoDadosFinanceirosDTO.class);
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
    protected Mono<Optional<AcaoEntity>> findByTicker(String t) {
        return Mono.fromCallable(() -> repo.findByTicker(t)).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    protected Mono<AcaoDadosFinanceirosDTO> scrape(String t) {
        return scraper.scrape(t);
    }

    @Override
    protected boolean isCacheValid(AcaoEntity e, Duration maxAge) {
        return Duration.between(e.getDataAtualizacao(), LocalDateTime.now()).compareTo(maxAge) < 0;
    }

    @Override
    protected Acao toDomain(AcaoDadosFinanceirosDTO raw) {
        return scraperMapper.toDomain(raw);
    }

    @Override
    protected AcaoEntity toEntity(Acao domain) {
        return persistenceMapper.toEntity(domain);
    }

    @Override
    protected Acao entityToDomain(AcaoEntity acaoEntity) {
        return scraperMapper.toDomain(acaoEntity);
    }

    @Override
    protected void enrichEntity(AcaoEntity entity, AcaoDadosFinanceirosDTO raw) {
        serializeRawInto(entity, raw); // só grava dadosBrutosJson
    }

    @Override
    protected void mergeForUpdate(AcaoEntity existente, AcaoEntity mapped) {
        mapped.setId(existente.getId()); // garante UPDATE
    }

    @Override
    protected AcaoEntity save(AcaoEntity e) {
        return repo.save(e);
    }

    @Override
    protected LocalDateTime entityUpdatedAt(AcaoEntity e) {
        return e.getDataAtualizacao();
    }

    @Override
    protected String entityRawJson(AcaoEntity e) {
        return e.getDadosBrutosJson();
    }

    @Override
    protected void setEntityRawJson(AcaoEntity e, String json) {
        e.setDadosBrutosJson(json);
    }
}
