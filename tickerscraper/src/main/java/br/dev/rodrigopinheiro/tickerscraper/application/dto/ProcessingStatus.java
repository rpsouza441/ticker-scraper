package br.dev.rodrigopinheiro.tickerscraper.application.dto;

/**
 * Status de processamento para operações de scraping.
 * Compartilhado entre diferentes tipos de ativos (Ação, FII, etc.).
 */
public enum ProcessingStatus {
    SUCCESS("Dados coletados com sucesso"),
    PARTIAL("Dados parcialmente coletados"),
    FAILED("Falha na coleta de dados"),
    CACHED("Dados obtidos do cache"),
    FALLBACK("Dados obtidos via fallback");
    
    private final String description;
    
    ProcessingStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isSuccessful() {
        return this == SUCCESS || this == CACHED;
    }
}