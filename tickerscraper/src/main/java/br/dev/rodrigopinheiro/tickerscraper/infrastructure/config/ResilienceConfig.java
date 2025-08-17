package br.dev.rodrigopinheiro.tickerscraper.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Resilience4j para monitoramento e logging de eventos.
 * Registra listeners para Circuit Breaker, Retry e TimeLimiter.
 */
@Configuration
public class ResilienceConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ResilienceConfig.class);
    
    /**
     * Registra eventos do Circuit Breaker para logging e monitoramento.
     */
    @Bean
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerEventConsumer() {
        return new RegistryEventConsumer<CircuitBreaker>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                CircuitBreaker circuitBreaker = entryAddedEvent.getAddedEntry();
                logger.info("Circuit Breaker '{}' registrado", circuitBreaker.getName());
                
                // Registrar eventos de mudança de estado
                circuitBreaker.getEventPublisher()
                    .onStateTransition(event -> {
                        logger.info("[{}] Circuit Breaker mudou de estado: {} -> {}", 
                                   circuitBreaker.getName(), 
                                   event.getStateTransition().getFromState(),
                                   event.getStateTransition().getToState());
                    })
                    .onCallNotPermitted(event -> {
                        logger.warn("[{}] Chamada rejeitada pelo Circuit Breaker (estado: {})", 
                                   circuitBreaker.getName(), 
                                   circuitBreaker.getState());
                    })
                    .onFailureRateExceeded(event -> {
                        logger.error("[{}] Taxa de falha excedida: {}%", 
                                    circuitBreaker.getName(), 
                                    event.getFailureRate());
                    })
                    .onSlowCallRateExceeded(event -> {
                        logger.warn("[{}] Taxa de chamadas lentas excedida: {}%", 
                                   circuitBreaker.getName(), 
                                   event.getSlowCallRate());
                    });
            }
            
            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
                logger.info("Circuit Breaker '{}' removido", entryRemoveEvent.getRemovedEntry().getName());
            }
            
            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
                logger.info("Circuit Breaker '{}' substituído", entryReplacedEvent.getNewEntry().getName());
            }
        };
    }
}