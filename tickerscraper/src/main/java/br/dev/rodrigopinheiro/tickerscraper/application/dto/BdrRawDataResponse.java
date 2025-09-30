package br.dev.rodrigopinheiro.tickerscraper.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BdrRawDataResponse(
        String ticker,
        String source,
        LocalDateTime scrapingTimestamp,
        ProcessingStatus processingStatus,
        Map<String, Object> rawData,
        Map<String, Object> metadata,
        String error // Apenas em caso de falha
) {

    // Factory method para sucesso
    public static BdrRawDataResponse success(
            String ticker,
            Map<String, Object> rawData,
            String source,
            Map<String, Object> metadata
    ) {
        return new BdrRawDataResponse(
                ticker,
                source,
                LocalDateTime.now(),
                ProcessingStatus.SUCCESS,
                rawData,
                metadata,
                null // sem erro
        );
    }

    // Factory method para parcial
    public static BdrRawDataResponse partial(
            String ticker,
            Map<String, Object> rawData,
            String source,
            Map<String, Object> metadata,
            String partialReason
    ) {
        Map<String, Object> enhancedMetadata = new java.util.HashMap<>(metadata);
        enhancedMetadata.put("partial_reason", partialReason);

        return new BdrRawDataResponse(
                ticker,
                source,
                LocalDateTime.now(),
                ProcessingStatus.PARTIAL,
                rawData,
                enhancedMetadata,
                null // sem erro
        );
    }

    // Factory method para falha
    public static BdrRawDataResponse failed(String ticker, String source, String errorMessage) {
        return new BdrRawDataResponse(
                ticker,
                source,
                LocalDateTime.now(),
                ProcessingStatus.FAILED,
                Map.of(), // rawData vazio
                Map.of("error_timestamp", LocalDateTime.now().toString()),
                errorMessage
        );
    }
}