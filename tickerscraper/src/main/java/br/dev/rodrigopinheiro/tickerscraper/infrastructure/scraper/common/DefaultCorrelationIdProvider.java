package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.common;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.config.CorrelationIdInterceptor;
import org.springframework.stereotype.Component;

/**
 * Implementação padrão do CorrelationIdProvider.
 * Delega para o CorrelationIdInterceptor existente.
 */
@Component
public class DefaultCorrelationIdProvider implements CorrelationIdProvider {
    
    @Override
    public String getCurrentCorrelationId() {
        try {
            return CorrelationIdInterceptor.getCurrentCorrelationId();
        } catch (Exception e) {
            // Em caso de erro, retorna null para não quebrar o fluxo
            return null;
        }
    }
}