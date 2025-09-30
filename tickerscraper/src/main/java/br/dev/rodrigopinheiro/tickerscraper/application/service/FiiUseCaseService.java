package br.dev.rodrigopinheiro.tickerscraper.application.service;


import br.dev.rodrigopinheiro.tickerscraper.application.dto.FiiRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.mapper.RawDataMapper;
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
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
public class FiiUseCaseService
        extends AbstractTickerUseCaseService<FiiDadosFinanceirosDTO, FundoImobiliario, FiiRawDataResponse>
        implements FiiUseCasePort {

    private final FiiDataScrapperPort scraper;
    private final FiiRepositoryPort repo;
    private final FiiScraperMapper scraperMapper;
    private final RawDataMapper rawDataMapper;

    public FiiUseCaseService(@Qualifier("fiiPlaywrightDirectScraper") FiiDataScrapperPort scraper,
                             FiiRepositoryPort repo,
                             FiiScraperMapper scraperMapper,
                             RawDataMapper rawDataMapper,
                             ObjectMapper objectMapper) {
        super(objectMapper, Duration.ofDays(1), FiiDadosFinanceirosDTO.class);
        this.scraper = scraper;
        this.repo = repo;
        this.scraperMapper = scraperMapper;
        this.rawDataMapper = rawDataMapper;
    }
    @Override protected String normalize(String t) { return t == null ? null : t.trim().toUpperCase(); }

    @Override 
    @Transactional(readOnly = true)
    protected Mono<Optional<FundoImobiliario>> findByTicker(String t) {
        // USE o method com dividendos (fetch join no adapter JPA)
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
    
    /**
     * Implementação da interface para retornar dados brutos como DTO da application.
     * Utiliza template method da classe pai para conversão consistente.
     */
    @Override
    public Mono<FiiRawDataResponse> getRawTickerData(String ticker) {
        return super.getRawTickerDataAsResponse(ticker);
    }
    
    /**
     * Converte DTO de infraestrutura para DTO da application usando MapStruct.
     */
    @Override
    protected FiiRawDataResponse convertToRawResponse(FiiDadosFinanceirosDTO infraDto) {
        // Para FII, não temos URLs de API capturadas neste contexto, então passamos Map vazio
        return rawDataMapper.toFiiRawDataResponse(infraDto, Map.of());
    }
    
    /**
     * Cria resposta de falha específica para FII.
     */
    @Override
    protected FiiRawDataResponse createFailedResponse(String ticker, String source, String error) {
        return rawDataMapper.createFailedFiiResponse(ticker, source, error);
    }
}