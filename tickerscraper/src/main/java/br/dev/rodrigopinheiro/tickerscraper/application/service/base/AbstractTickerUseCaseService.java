package br.dev.rodrigopinheiro.tickerscraper.application.service.base;


import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Optional;

public abstract class AbstractTickerUseCaseService<RAW, DOMAIN, RAW_RESPONSE> {

    private final ObjectMapper json;
    private final Duration maxCacheAge;
    private final Class<RAW> rawClass;

    protected AbstractTickerUseCaseService(ObjectMapper json, Duration maxCacheAge, Class<RAW> rawClass) {
        this.json = json;
        this.maxCacheAge = maxCacheAge;
        this.rawClass = rawClass;
    }

    public Mono<DOMAIN> getTickerData(String ticker) {
        final String t = normalize(ticker);
        return findByTicker(t)
                .flatMap(opt -> {
                    if (opt.isPresent() && isCacheValid(opt.get(), maxCacheAge)) {
                        return Mono.just(opt.get());
                    }
                    return scrape(t).flatMap(raw -> persistFromRaw(raw));
                });
    }

    protected Mono<RAW> getRawInfrastructureData(String ticker) {
        final String t = normalize(ticker);
        return findByTicker(t)
                .flatMap(opt -> {
                    if (opt.isPresent() && isCacheValid(opt.get(), maxCacheAge)) {
                        return readRawFromStore(t).switchIfEmpty(scrape(t));
                    }
                    return scrape(t)
                            .flatMap(raw -> persistFromRaw(raw).thenReturn(raw));
                });
    }
    
    /**
     * Template method para obter dados brutos como DTO da application.
     * Implementa o padrão Template Method para conversão consistente.
     */
    protected Mono<RAW_RESPONSE> getRawTickerDataAsResponse(String ticker) {
        final String normalizedTicker = normalize(ticker);
        
        return getRawInfrastructureData(normalizedTicker)
                .map(this::convertToRawResponse)
                .onErrorReturn(createFailedResponse(normalizedTicker, "SCRAPER", "Falha na coleta de dados"));
    }

    // ---------- core persist (agora retorna DOMAIN) ----------
    protected Mono<DOMAIN> persistFromRaw(RAW raw) {
        return Mono.fromCallable(() -> {
            DOMAIN domain = toDomain(raw);          // RAW -> DOMAIN
            return saveDomain(domain, raw);         // persiste e retorna DOMAIN
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ---------- hooks a serem implementados ----------
    protected abstract String normalize(String ticker);
    protected abstract Mono<Optional<DOMAIN>> findByTicker(String ticker);
    protected abstract Mono<RAW> scrape(String ticker);
    protected abstract boolean isCacheValid(DOMAIN domain, Duration maxAge);
    protected abstract DOMAIN toDomain(RAW raw);
    protected abstract DOMAIN saveDomain(DOMAIN domain, RAW raw);
    
    // ---------- hooks para template method de raw response ----------
    /**
     * Converte DTO de infraestrutura para DTO da application.
     * Deve ser implementado pelas subclasses específicas.
     */
    protected abstract RAW_RESPONSE convertToRawResponse(RAW infraDto);
    
    /**
     * Cria resposta de falha específica para o tipo de ativo.
     * Deve ser implementado pelas subclasses específicas.
     */
    protected abstract RAW_RESPONSE createFailedResponse(String ticker, String source, String error);

    // opcional: leitura do RAW persistido (JSONB → RAW)
    protected Mono<RAW> readRawFromStore(String ticker) { return Mono.empty(); }

    // helper para subclasses
    protected RAW deserialize(String json) throws Exception {
        return this.json.readValue(json, rawClass);
    }
    protected String serialize(Object raw) throws Exception {
        return this.json.writeValueAsString(raw);
    }
}
