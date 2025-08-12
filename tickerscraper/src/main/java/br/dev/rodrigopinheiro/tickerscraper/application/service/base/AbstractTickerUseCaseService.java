package br.dev.rodrigopinheiro.tickerscraper.application.service.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public abstract class AbstractTickerUseCaseService<RAW, DOMAIN, ENTITY> {

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
                        // <-- CONVERTE ENTITY -> DOMAIN AQUI
                        return Mono.just(entityToDomain(opt.get()));
                    }
                    // <-- persistFromRaw AGORA DEVOLVE DOMAIN
                    return scrape(t).flatMap(raw -> persistFromRaw(raw, opt));
                });
    }

    public Mono<RAW> getRawTickerData(String ticker) {
        final String t = normalize(ticker);
        return findByTicker(t)
                .flatMap(opt -> {
                    if (opt.isPresent() && isCacheValid(opt.get(), maxCacheAge)) {
                        return deserialize(opt.get());
                    }
                    // persistFromRaw devolve DOMAIN, mas aqui só queremos devolver RAW ao caller
                    return scrape(t).flatMap(raw -> persistFromRaw(raw, opt).thenReturn(raw));
                });
    }

    // ---------- core persist (agora retorna DOMAIN) ----------
    protected Mono<DOMAIN> persistFromRaw(RAW raw, Optional<ENTITY> existente) {
        return Mono.fromCallable(() -> {
                    DOMAIN domain = toDomain(raw);          // RAW -> DOMAIN (regra / parser)
                    ENTITY mapped = toEntity(domain);       // DOMAIN -> ENTITY (adapter de persistência)
                    enrichEntity(mapped, raw);              // ex.: dadosBrutosJson, internalId
                    existente.ifPresent(prev -> mergeForUpdate(prev, mapped)); // política de update
                    ENTITY saved = save(mapped);            // persiste
                    return entityToDomain(saved);           // <-- volta para DOMAIN
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ---------- hooks (Ação / FII implementam) ----------
    protected abstract String normalize(String ticker);
    protected abstract Mono<Optional<ENTITY>> findByTicker(String ticker);
    protected abstract Mono<RAW> scrape(String ticker);
    protected abstract boolean isCacheValid(ENTITY entity, Duration maxAge);

    protected abstract DOMAIN toDomain(RAW raw);
    protected abstract ENTITY toEntity(DOMAIN domain);
    protected abstract DOMAIN entityToDomain(ENTITY entity); // <-- NOVO

    protected abstract void enrichEntity(ENTITY entity, RAW raw);
    protected abstract void mergeForUpdate(ENTITY existente, ENTITY mapped);
    protected abstract ENTITY save(ENTITY entity);

    protected abstract LocalDateTime entityUpdatedAt(ENTITY entity);
    protected abstract String entityRawJson(ENTITY entity);
    protected abstract void setEntityRawJson(ENTITY entity, String json);

    // ---------- util ----------
    protected Mono<RAW> deserialize(ENTITY entity) {
        return Mono.fromCallable(() -> json.readValue(entityRawJson(entity), rawClass))
                .subscribeOn(Schedulers.boundedElastic());
    }

    protected void serializeRawInto(ENTITY entity, RAW raw) {
        try {
            setEntityRawJson(entity, json.writeValueAsString(raw));
        } catch (JsonProcessingException e) {
            // auditoria falhou? prossiga
        }
    }
}
