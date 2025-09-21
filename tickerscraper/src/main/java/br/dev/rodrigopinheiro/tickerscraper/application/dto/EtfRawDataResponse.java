package br.dev.rodrigopinheiro.tickerscraper.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO da camada application para dados brutos de ETF (Exchange Traded Fund).
 * Representa dados não processados obtidos do scraping de ETFs,
 * mantendo a independência da camada de domínio em relação à infraestrutura.
 * 
 * @param ticker Código do ETF
 * @param rawData Dados brutos em formato chave-valor
 * @param source Fonte dos dados (Playwright, Selenium, API)
 * @param scrapingTimestamp Momento da coleta dos dados
 * @param processingStatus Status do processamento (SUCCESS, PARTIAL, FAILED)
 * @param metadata Metadados adicionais sobre a coleta
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EtfRawDataResponse(
        String ticker,
        Map<String, Object> rawData,
        String source,
        LocalDateTime scrapingTimestamp,
        ProcessingStatus processingStatus,
        Map<String, String> metadata
) {

    /**
     * Construtor para dados bem-sucedidos.
     */
    public static EtfRawDataResponse success(String ticker, Map<String, Object> rawData, String source) {
        return new EtfRawDataResponse(
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
    public static EtfRawDataResponse partial(String ticker, Map<String, Object> rawData, 
                                            String source, String failureReason) {
        return new EtfRawDataResponse(
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
    public static EtfRawDataResponse failed(String ticker, String source, String error) {
        return new EtfRawDataResponse(
                ticker,
                Map.of(),
                source,
                LocalDateTime.now(),
                ProcessingStatus.FAILED,
                Map.of("error", error)
        );
    }

    /**
     * Verifica se o processamento foi bem-sucedido.
     */
    public boolean isSuccess() {
        return processingStatus == ProcessingStatus.SUCCESS;
    }

    /**
     * Verifica se houve falha no processamento.
     */
    public boolean isFailed() {
        return processingStatus == ProcessingStatus.FAILED;
    }

    /**
     * Verifica se o processamento foi parcial.
     */
    public boolean isPartial() {
        return processingStatus == ProcessingStatus.PARTIAL;
    }
}