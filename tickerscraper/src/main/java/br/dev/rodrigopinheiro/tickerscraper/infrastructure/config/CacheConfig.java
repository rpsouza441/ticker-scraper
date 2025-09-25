package br.dev.rodrigopinheiro.tickerscraper.infrastructure.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CacheConfig {
    
    private final TickerClassificationProperties properties;
    
    @Bean
    public Cache<String, TipoAtivo> tickerClassificationCache() {
        var cacheConfig = properties.getCache();
        
        if (!cacheConfig.isEnabled()) {
            log.warn("Cache de classificação está DESABILITADO via configuração");
            // Retorna cache com tamanho 0 (efetivamente desabilitado)
            return Caffeine.newBuilder().maximumSize(0).build();
        }
        
        Cache<String, TipoAtivo> cache = Caffeine.newBuilder()
            .maximumSize(cacheConfig.getMaxSize())
            .expireAfterWrite(cacheConfig.getTtlHours(), TimeUnit.HOURS)
            .recordStats()
            .build();
        
        log.info("Cache de classificação inicializado: enabled={}, max={}, TTL={}h", 
            cacheConfig.isEnabled(), cacheConfig.getMaxSize(), cacheConfig.getTtlHours());
        return cache;
    }
}
