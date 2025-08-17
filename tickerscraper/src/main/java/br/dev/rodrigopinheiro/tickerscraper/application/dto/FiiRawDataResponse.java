package br.dev.rodrigopinheiro.tickerscraper.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO da camada application para dados brutos de FII (Fundo Imobiliário).
 * Representa dados não processados obtidos do scraping de FIIs,
 * mantendo a independência da camada de domínio em relação à infraestrutura.
 * 
 * @param ticker Código do FII
 * @param rawData Dados brutos em formato chave-valor
 * @param source Fonte dos dados (Playwright, Selenium, API)
 * @param scrapingTimestamp Momento da coleta dos dados
 * @param processingStatus Status do processamento (SUCCESS, PARTIAL, FAILED)
 * @param metadata Metadados adicionais sobre a coleta
 * @param apiUrls URLs das APIs capturadas durante o scraping
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FiiRawDataResponse(
        String ticker,
        Map<String, Object> rawData,
        String source,
        LocalDateTime scrapingTimestamp,
        ProcessingStatus processingStatus,
        Map<String, String> metadata,
        Map<String, String> apiUrls  // URLs específicas para FIIs
) {

    

    /**
     * Construtor simplificado para sucesso completo.
     */
    public static FiiRawDataResponse success(String ticker, Map<String, Object> rawData, 
                                           String source, Map<String, String> apiUrls) {
        return new FiiRawDataResponse(
                ticker,
                rawData,
                source,
                LocalDateTime.now(),
                ProcessingStatus.SUCCESS,
                Map.of(
                    "total_fields", String.valueOf(rawData.size()),
                    "apis_captured", String.valueOf(apiUrls.size())
                ),
                apiUrls
        );
    }
    
    /**
     * Construtor para dados parciais com informações de falha.
     */
    public static FiiRawDataResponse partial(String ticker, Map<String, Object> rawData, 
                                           String source, Map<String, String> apiUrls, 
                                           String failureReason) {
        return new FiiRawDataResponse(
                ticker,
                rawData,
                source,
                LocalDateTime.now(),
                ProcessingStatus.PARTIAL,
                Map.of(
                    "total_fields", String.valueOf(rawData.size()),
                    "apis_captured", String.valueOf(apiUrls.size()),
                    "failure_reason", failureReason
                ),
                apiUrls
        );
    }
    
    /**
     * Construtor para falha completa.
     */
    public static FiiRawDataResponse failed(String ticker, String source, String errorMessage) {
        return new FiiRawDataResponse(
                ticker,
                Map.of(),
                source,
                LocalDateTime.now(),
                ProcessingStatus.FAILED,
                Map.of("error_message", errorMessage),
                Map.of()
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
     * Verifica se APIs foram capturadas durante o scraping.
     */
    public boolean hasApiUrls() {
        return apiUrls != null && !apiUrls.isEmpty();
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
    
    /**
     * Obtém URL de uma API específica capturada.
     */
    public String getApiUrl(String apiType) {
        return apiUrls != null ? apiUrls.get(apiType) : null;
    }
    
    /**
     * Verifica se uma API específica foi capturada.
     */
    public boolean hasApiUrl(String apiType) {
        return getApiUrl(apiType) != null;
    }
}