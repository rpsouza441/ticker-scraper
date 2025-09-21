package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.EtfRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.mapper.RawDataMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.EtfUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.EtfScraperPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.EtfRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.service.base.AbstractTickerUseCaseService;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Etf;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.dto.EtfDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.mapper.EtfScraperMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementação dos casos de uso para ETFs.
 * Estende AbstractTickerUseCaseService para reutilizar lógica comum.
 */
@Service
public class EtfUseCaseService extends AbstractTickerUseCaseService<EtfDadosFinanceirosDTO, Etf, EtfRawDataResponse> 
        implements EtfUseCasePort {

    private final EtfScraperPort scraper;
    private final EtfRepositoryPort repo;
    private final EtfScraperMapper scraperMapper;
    private final RawDataMapper rawDataMapper;

    public EtfUseCaseService(@Qualifier("etfPlaywrightScraper") EtfScraperPort scraper,
                            EtfRepositoryPort repo,
                            EtfScraperMapper scraperMapper,
                            RawDataMapper rawDataMapper,
                            ObjectMapper objectMapper) {
        super(objectMapper, Duration.ofDays(1), EtfDadosFinanceirosDTO.class);
        this.scraper = scraper;
        this.repo = repo;
        this.scraperMapper = scraperMapper;
        this.rawDataMapper = rawDataMapper;
    }

    /**
     * Implementação da interface para retornar dados processados como entidade de domínio.
     */
    @Override
    public Mono<Etf> getTickerData(String ticker) {
        return super.getTickerData(ticker);
    }

    /**
     * Implementação da interface para retornar dados brutos como DTO da application.
     */
    @Override
    public Mono<EtfRawDataResponse> getRawTickerData(String ticker) {
        return super.getRawTickerDataAsResponse(ticker);
    }

    /**
     * Normaliza o ticker para ETF (sempre maiúsculo e sem espaços).
     */
    @Override
    protected String normalize(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            return null;
        }
        return ticker.trim().toUpperCase();
    }

    /**
     * Busca ETF no repositório por ticker.
     */
    @Override
    protected Mono<Optional<Etf>> findByTicker(String ticker) {
        return Mono.fromCallable(() -> repo.findByTicker(ticker))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * Realiza scraping específico para ETF.
     */
    @Override
    protected Mono<EtfDadosFinanceirosDTO> scrape(String ticker) {
        return scraper.scrapeEtfData(ticker);
    }

    /**
     * Valida se o cache do ETF ainda é válido.
     */
    @Override
    protected boolean isCacheValid(Etf etf, Duration maxAge) {
        LocalDateTime updatedAt = etf.getDataAtualizacao();
        return updatedAt != null &&
                Duration.between(updatedAt, LocalDateTime.now()).compareTo(maxAge) < 0;
    }

    /**
     * Converte DTO de infraestrutura para entidade de domínio.
     */
    @Override
    protected Etf toDomain(EtfDadosFinanceirosDTO scrapedData) {
        return scraperMapper.toDomain(scrapedData);
    }

    /**
     * Salva a entidade de domínio no repositório.
     */
    @Override
    protected Etf saveDomain(Etf etf, EtfDadosFinanceirosDTO rawData) {
        etf.setDataAtualizacao(LocalDateTime.now());
        String audit = null;
        try { 
            audit = serialize(rawData); 
        } catch (Exception ignored) {}
        return repo.save(etf, audit != null ? audit : "{}");
    }

    /**
     * Converte DTO de infraestrutura para DTO da application usando MapStruct.
     */
    @Override
    protected EtfRawDataResponse convertToRawResponse(EtfDadosFinanceirosDTO infraDto) {
        return rawDataMapper.toEtfRawDataResponse(infraDto);
    }

    /**
     * Cria resposta de falha específica para ETF.
     */
    @Override
    protected EtfRawDataResponse createFailedResponse(String ticker, String source, String error) {
        return rawDataMapper.createFailedEtfResponse(ticker, source, error);
    }
}