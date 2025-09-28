package br.dev.rodrigopinheiro.tickerscraper.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BdrRawDataResponse(
        String ticker,
        Map<String, Object> rawData,
        String source,
        LocalDateTime scrapingTimestamp,
        ProcessingStatus processingStatus,
        Map<String, String> metadata,
        Map<String, String> apiUrls
) {
    public static BdrRawDataResponse success(String ticker, Map<String, Object> rawData,
                                             String source, Map<String, String> apiUrls) {
        return new BdrRawDataResponse(
                ticker, rawData, source, LocalDateTime.now(), ProcessingStatus.SUCCESS,
                Map.of("total_fields", String.valueOf(rawData.size()),
                        "apis_captured", String.valueOf(apiUrls.size())),
                apiUrls
        );
    }
    public static BdrRawDataResponse partial(String ticker, Map<String, Object> rawData,
                                             String source, Map<String, String> apiUrls,
                                             String failureReason) {
        return new BdrRawDataResponse(
                ticker, rawData, source, LocalDateTime.now(), ProcessingStatus.PARTIAL,
                Map.of("total_fields", String.valueOf(rawData.size()),
                        "apis_captured", String.valueOf(apiUrls.size()),
                        "failure_reason", failureReason),
                apiUrls
        );
    }
    public static BdrRawDataResponse failed(String ticker, String source, String errorMessage) {
        return new BdrRawDataResponse(
                ticker, Map.of(), source, LocalDateTime.now(), ProcessingStatus.FAILED,
                Map.of("error_message", errorMessage), Map.of()
        );
    }
}
