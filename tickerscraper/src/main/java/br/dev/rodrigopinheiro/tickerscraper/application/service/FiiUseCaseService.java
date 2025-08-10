package br.dev.rodrigopinheiro.tickerscraper.application.service;


import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FiiDividendoEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.FiiJpaReporitoty;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.FiiPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.FiiUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.mapper.FiiScraperMapper;
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
import java.util.List;
import java.util.Optional;

@Service
public class FiiUseCaseService implements FiiUseCasePort {
    private static final Logger logger = LoggerFactory.getLogger(FiiUseCaseService.class);

    private final FiiDataScrapperPort scraper;
    private final FiiScraperMapper fiiScraperMapper;
    private final FiiPersistenceMapper fiiPersistenceMapper;
    private final FiiJpaReporitoty fiiRepository;
    private final ObjectMapper json = new ObjectMapper();

    public FiiUseCaseService(
            @Qualifier("fiiPlaywrightDirectScraper") FiiDataScrapperPort scraper,
            FiiScraperMapper fiiScraperMapper,
            FiiPersistenceMapper fiiPersistenceMapper,
            FiiJpaReporitoty fiiRepository
    ) {
        this.scraper = scraper;
        this.fiiScraperMapper = fiiScraperMapper;
        this.fiiPersistenceMapper = fiiPersistenceMapper;
        this.fiiRepository = fiiRepository;
    }

    @Override
    public Mono<FundoImobiliarioEntity> getTickerData(String ticker) {
        final String t = ticker.toUpperCase().trim();

        return Mono.fromCallable(() -> fiiRepository.findByTicker(t))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(opt -> {
                    if (opt.isPresent() && isCacheValid(opt.get())) {
                        return Mono.just(opt.get());
                    }
                    // missing ou stale -> scrape + save -> retorna entity
                    return scraper.scrape(t)
                            .flatMap(dto -> Mono.fromCallable(() -> persistFromDto(dto, opt))
                                    .subscribeOn(Schedulers.boundedElastic()));
                });
    }

    @Override
    public Mono<FiiDadosFinanceirosDTO> getRawTickerData(String ticker) {
        final String t = ticker.toUpperCase().trim();

        return Mono.fromCallable(() -> fiiRepository.findByTicker(t))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(opt -> {
                    if (opt.isPresent() && isCacheValid(opt.get())) {
                        // cache válido -> devolve o bruto do banco
                        return Mono.fromCallable(() ->
                                json.readValue(opt.get().getDadosBrutosJson(), FiiDadosFinanceirosDTO.class)
                        ).subscribeOn(Schedulers.boundedElastic());
                    }
                    // missing ou stale -> scrape + save -> retorna o bruto recém-scrapeado
                    return scraper.scrape(t)
                            .flatMap(dto -> Mono.fromCallable(() -> {
                                        persistFromDto(dto, opt);
                                        return dto;
                                    })
                                    .subscribeOn(Schedulers.boundedElastic()));
                });
    }


    /** Persiste a partir do DTO de scraping (atualiza se já existir). */
    private FundoImobiliarioEntity persistFromDto(FiiDadosFinanceirosDTO dto, Optional<FundoImobiliarioEntity> existenteOpt) {
        // 1) DTO -> domínio
        FundoImobiliario domain = fiiScraperMapper.toDomain(dto);

        // 2) domínio -> entity
        FundoImobiliarioEntity entity = fiiPersistenceMapper.toEntity(domain);

        // 3) internalId (obrigatório na tabela) vem do DTO
        if (dto.internalId() == null) {
            throw new IllegalArgumentException("internalId é obrigatório para persistir FII.");
        }
        entity.setInternalId(dto.internalId().longValue());

        // 4) back-ref dividendos
        List<FiiDividendoEntity> divs = fiiPersistenceMapper.toEntityList(domain.getFiiDividendos());
        divs.forEach(d -> d.setFundoImobiliario(entity));
        entity.setFiiDividendos(divs);

        // 5) serializa bruto
        try {
            entity.setDadosBrutosJson(json.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            logger.warn("Falha ao serializar JSON bruto para {}", domain.getTicker(), e);
        }

        // 6) update se já existia
        existenteOpt.ifPresent(ex -> entity.setId(ex.getId()));

        // 7) save
        return fiiRepository.save(entity);
    }

    private boolean isCacheValid(FundoImobiliarioEntity e) {
        final Duration maxAge = Duration.ofDays(1);
        return e.getDataAtualizacao() != null
                && Duration.between(e.getDataAtualizacao(), LocalDateTime.now()).compareTo(maxAge) < 0;
    }
}