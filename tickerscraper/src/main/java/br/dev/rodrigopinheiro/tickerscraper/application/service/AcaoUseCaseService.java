package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.AcaoRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.mapper.RawDataMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.AcaoUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.service.base.AbstractTickerUseCaseService;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.mapper.AcaoScraperMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AcaoUseCaseService
        extends AbstractTickerUseCaseService<AcaoDadosFinanceirosDTO, Acao, AcaoRawDataResponse>
        implements AcaoUseCasePort {

    private final AcaoDataScrapperPort scraper;
    private final AcaoRepositoryPort repo;
    private final AcaoScraperMapper scraperMapper;
    private final RawDataMapper rawDataMapper;

    public AcaoUseCaseService(@Qualifier("acaoPlaywrightScraper") AcaoDataScrapperPort scraper,
                              AcaoRepositoryPort repo,
                              AcaoScraperMapper scraperMapper,
                              RawDataMapper rawDataMapper,
                              ObjectMapper json) {
        super(json, Duration.ofDays(1), AcaoDadosFinanceirosDTO.class);
        this.scraper = scraper;
        this.repo = repo;
        this.scraperMapper = scraperMapper;
        this.rawDataMapper = rawDataMapper;
    }

    @Override protected String normalize(String t) { return t == null ? null : t.trim().toUpperCase(); }

    @Override
    protected Mono<Optional<Acao>> findByTicker(String t) {
        return Mono.fromCallable(() -> repo.findByTicker(t))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    protected Mono<AcaoDadosFinanceirosDTO> scrape(String t) {
        return scraper.scrape(t);
    }

    @Override
    protected boolean isCacheValid(Acao d, Duration maxAge) {
        LocalDateTime updatedAt = d.getDataAtualizacao();
        return updatedAt != null &&
                Duration.between(updatedAt, LocalDateTime.now()).compareTo(maxAge) < 0;
    }

    @Override
    protected Acao toDomain(AcaoDadosFinanceirosDTO raw) {
        return scraperMapper.toDomain(raw);
    }

    @Override
    protected Acao saveDomain(Acao domain, AcaoDadosFinanceirosDTO raw) {
        String audit = null;
        try { audit = serialize(raw); } catch (Exception ignored) {}
        return repo.save(domain, audit);
    }

    @Override
    protected Mono<AcaoDadosFinanceirosDTO> readRawFromStore(String ticker) {
        return Mono.fromCallable(() -> repo.findRawJsonByTicker(ticker))
                .flatMap(opt -> opt
                        .map(json -> {
                            try { return Mono.just(deserialize(json)); }
                            catch (Exception e) { return Mono.<AcaoDadosFinanceirosDTO>error(e); }
                        })
                        .orElseGet(Mono::empty))
                .subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Implementação da interface para retornar dados brutos como DTO da application.
     * Utiliza template method da classe pai para conversão consistente.
     */
    @Override
    public Mono<AcaoRawDataResponse> getRawTickerData(String ticker) {
        return super.getRawTickerDataAsResponse(ticker);
    }
    
    /**
     * Converte DTO de infraestrutura para DTO da application usando MapStruct.
     */
    @Override
    protected AcaoRawDataResponse convertToRawResponse(AcaoDadosFinanceirosDTO infraDto) {
        return rawDataMapper.toAcaoRawDataResponse(infraDto);
    }
    
    /**
     * Cria resposta de falha específica para Ação.
     */
    @Override
    protected AcaoRawDataResponse createFailedResponse(String ticker, String source, String error) {
        return rawDataMapper.createFailedAcaoResponse(ticker, source, error);
    }
}
