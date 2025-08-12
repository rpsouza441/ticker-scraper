package br.dev.rodrigopinheiro.tickerscraper.application.service.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Optional;

public abstract class AbstractTickerUseCaseService<RAW, DOMAIN> {

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

    public Mono<RAW> getRawTickerData(String ticker) {
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

    // ---------- core persist (agora retorna DOMAIN) ----------
    protected Mono<DOMAIN> persistFromRaw(RAW raw) {
        return Mono.fromCallable(() -> {
                    DOMAIN domain = toDomain(raw);          // RAW -> DOMAIN
                    return saveDomain(domain, raw);         // persiste e retorna DOMAIN
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ---------- hooks a serem implementados ----------
    protected abstract String normalize(String ticker);
    protected abstract Mono<Optional<DOMAIN>> findByTicker(String ticker);
    protected abstract Mono<RAW> scrape(String ticker);
    protected abstract boolean isCacheValid(DOMAIN domain, Duration maxAge);
    protected abstract DOMAIN toDomain(RAW raw);
    protected abstract DOMAIN saveDomain(DOMAIN domain, RAW raw);

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
