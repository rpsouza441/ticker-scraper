package br.dev.rodrigopinheiro.tickerscraper.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para transporte de dados brutos de BDRs entre a aplicação e a camada de apresentação.
 *
 * @param ticker código do BDR
 * @param investidorId identificador da sessão na Investidor10 (quando disponível)
 * @param rawData estrutura com dados já pré-normalizados (cotação, dividendos, indicadores, etc.)
 * @param apiPayloads payloads capturados das APIs externas durante o scraping
 * @param source fonte do scraping (playwright, selenium...)
 * @param scrapingTimestamp timestamp da coleta
 * @param processingStatus status geral da captura (SUCCESS, PARTIAL, FAILED)
 * @param metadata metadados adicionais sobre a execução
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BdrRawDataResponse(
        String ticker,
        String investidorId,
        Map<String, Object> rawData,
        Map<String, String> apiPayloads,
        String source,
        LocalDateTime scrapingTimestamp,
        ProcessingStatus processingStatus,
        Map<String, String> metadata
) {
}
