package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.common;

/**
 * Interface para prover correlationId de forma desacoplada.
 * Permite injeção de dependência e facilita testes unitários.
 * 
 * Implementa o Dependency Inversion Principle (DIP) do SOLID.
 */
public interface CorrelationIdProvider {
    
    /**
     * Obtém o correlationId atual da requisição.
     * 
     * @return correlationId atual ou null se não disponível
     */
    String getCurrentCorrelationId();
    
    /**
     * Obtém o correlationId atual ou um valor padrão.
     * 
     * @param defaultValue valor padrão se correlationId não estiver disponível
     * @return correlationId atual ou valor padrão
     */
    default String getCurrentCorrelationIdOrDefault(String defaultValue) {
        String correlationId = getCurrentCorrelationId();
        return correlationId != null ? correlationId : defaultValue;
    }
}