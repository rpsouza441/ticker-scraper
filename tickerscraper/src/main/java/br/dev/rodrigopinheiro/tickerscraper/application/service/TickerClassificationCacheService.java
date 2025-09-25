package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class TickerClassificationCacheService {
    
    private final Cache<String, TipoAtivo> cache;
    
    public TickerClassificationCacheService(Cache<String, TipoAtivo> cache) {
        this.cache = cache;
    }
    
    /**
     * Busca classificação no cache
     */
    public Optional<TipoAtivo> get(String ticker) {
        var resultado = Optional.ofNullable(cache.getIfPresent(ticker.toUpperCase()));
        
        if (resultado.isPresent()) {
            log.debug("Cache HIT para ticker {}: {}", ticker, resultado.get());
        } else {
            log.debug("Cache MISS para ticker {}", ticker);
        }
        
        return resultado;
    }
    
    /**
     * Armazena classificação no cache
     */
    public void put(String ticker, TipoAtivo tipo) {
        cache.put(ticker.toUpperCase(), tipo);
        log.debug("Cache STORE: {} -> {}", ticker, tipo);
    }
    
    /**
     * Remove entrada do cache
     */
    public void evict(String ticker) {
        cache.invalidate(ticker.toUpperCase());
        log.debug("Cache EVICT: {}", ticker);
    }
    
    /**
     * Limpa todo o cache
     */
    public void clear() {
        cache.invalidateAll();
        log.info("Cache limpo completamente");
    }
    
    /**
     * Estatísticas do cache
     */
    public String getStats() {
        var stats = cache.stats();
        return String.format(
            "Cache Stats - Hits: %d, Misses: %d, Hit Rate: %.2f%%, Size: %d",
            stats.hitCount(),
            stats.missCount(), 
            stats.hitRate() * 100,
            cache.estimatedSize()
        );
    }
}