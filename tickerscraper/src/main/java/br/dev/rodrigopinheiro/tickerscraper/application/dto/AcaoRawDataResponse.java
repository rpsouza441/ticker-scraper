package br.dev.rodrigopinheiro.tickerscraper.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO da camada application para dados brutos de ação.
 * Representa dados não processados obtidos do scraping, 
 * mantendo a independência da camada de domínio em relação à infraestrutura.
 * 
 * @param ticker Código da ação
 * @param rawData Dados brutos em formato chave-valor
 * @param source Fonte dos dados (Playwright, Selenium, API)
 * @param scrapingTimestamp Momento da coleta dos dados
 * @param processingStatus Status do processamento (SUCCESS, PARTIAL, FAILED)
 * @param metadata Metadados adicionais sobre a coleta
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AcaoRawDataResponse(
        String ticker,
        Map<String, Object> rawData,
        String source,
        LocalDateTime scrapingTimestamp,
        ProcessingStatus processingStatus,
        Map<String, String> metadata
) {
    

    /**
     * Construtor simplificado para sucesso completo.
     */
    public static AcaoRawDataResponse success(String ticker, Map<String, Object> rawData, String source) {
        return new AcaoRawDataResponse(
                ticker,
                rawData,
                source,
                LocalDateTime.now(),
                ProcessingStatus.SUCCESS,
                Map.of("total_fields", String.valueOf(rawData.size()))
        );
    }
    
    /**
     * Construtor para dados parciais com informações de falha.
     */
    public static AcaoRawDataResponse partial(String ticker, Map<String, Object> rawData, 
                                            String source, String failureReason) {
        return new AcaoRawDataResponse(
                ticker,
                rawData,
                source,
                LocalDateTime.now(),
                ProcessingStatus.PARTIAL,
                Map.of(
                    "total_fields", String.valueOf(rawData.size()),
                    "failure_reason", failureReason
                )
        );
    }
    
    /**
     * Construtor para falha completa.
     */
    public static AcaoRawDataResponse failed(String ticker, String source, String errorMessage) {
        return new AcaoRawDataResponse(
                ticker,
                Map.of(),
                source,
                LocalDateTime.now(),
                ProcessingStatus.FAILED,
                Map.of("error_message", errorMessage)
        );
    }
    
    /**
     * Verifica se a coleta foi bem-sucedida.
     */
    public boolean isSuccessful() {
        return processingStatus == ProcessingStatus.SUCCESS;
    }
    
    /**
     * Verifica se há dados disponíveis (mesmo que parciais).
     */
    public boolean hasData() {
        return rawData != null && !rawData.isEmpty();
    }
    
    /**
     * Obtém um valor específico dos dados brutos com type safety.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key, Class<T> type) {
        Object value = rawData.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
}