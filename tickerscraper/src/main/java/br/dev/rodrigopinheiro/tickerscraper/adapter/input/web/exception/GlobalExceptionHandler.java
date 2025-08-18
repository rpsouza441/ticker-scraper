package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.exception;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.exception.dto.ErrorResponse;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.config.CorrelationIdInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Handler global para tratamento centralizado de exceções da aplicação.
 * Converte exceções de domínio em responses HTTP apropriadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ScrapingTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleScrapingTimeout(ScrapingTimeoutException ex, WebRequest request) {
        logger.warn("[{}] Timeout no scraping: ticker={}, url={}, timeout={}s", 
                   getCorrelationId(), ex.getTicker(), ex.getUrl(), ex.getTimeout().getSeconds());
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getErrorCode())
                .message("Timeout durante o scraping. Tente novamente em alguns minutos.")
                .ticker(ex.getTicker())
                .correlationId(getCorrelationId())
                .timestamp(LocalDateTime.now())
                .retryable(ex.isRetryable())
                .details(Map.of(
                    "timeout_seconds", ex.getTimeout().getSeconds(),
                    "operation", ex.getOperation()
                ))
                .build();
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(error);
    }
    
    @ExceptionHandler(AntiBotDetectedException.class)
    public ResponseEntity<ErrorResponse> handleAntiBotDetected(AntiBotDetectedException ex, WebRequest request) {
        logger.error("[{}] Anti-bot detectado: ticker={}, reason={}, method={}", 
                    getCorrelationId(), ex.getTicker(), ex.getDetectionReason(), ex.getDetectionMethod());
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getErrorCode())
                .message("Acesso temporariamente bloqueado. Tente novamente mais tarde.")
                .ticker(ex.getTicker())
                .correlationId(getCorrelationId())
                .timestamp(LocalDateTime.now())
                .retryable(ex.isRetryable())
                .details(Map.of(
                    "detection_reason", ex.getDetectionReason(),
                    "detection_method", ex.getDetectionMethod(),
                    "try_alternative", ex.shouldTryAlternativeMethod()
                ))
                .build();
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }
    
    @ExceptionHandler(DataParsingException.class)
    public ResponseEntity<ErrorResponse> handleDataParsing(DataParsingException ex, WebRequest request) {
        logger.error("[{}] Erro de parsing: ticker={}, selector={}, expected={}", 
                    getCorrelationId(), ex.getTicker(), ex.getSelector(), ex.getExpectedData(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getErrorCode())
                .message("Falha ao processar dados do site. A estrutura pode ter mudado.")
                .ticker(ex.getTicker())
                .correlationId(getCorrelationId())
                .timestamp(LocalDateTime.now())
                .retryable(ex.isRetryable())
                .details(Map.of(
                    "selector", ex.getSelector(),
                    "expected_data", ex.getExpectedData(),
                    "actual_content", ex.getActualContent() != null ? ex.getActualContent() : "null"
                ))
                .build();
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }
    
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex, WebRequest request) {
        logger.warn("[{}] Rate limit excedido: ticker={}, requests={}, retry_after={}", 
                   getCorrelationId(), ex.getTicker(), ex.getRequestCount(), ex.getRetryAfter());
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getErrorCode())
                .message("Muitas requisições. Aguarde antes de tentar novamente.")
                .ticker(ex.getTicker())
                .correlationId(getCorrelationId())
                .timestamp(LocalDateTime.now())
                .retryable(ex.isRetryable())
                .details(Map.of(
                    "request_count", ex.getRequestCount(),
                    "retry_after", ex.getRetryAfter().toString(),
                    "wait_seconds", ex.getWaitTime().getSeconds()
                ))
                .build();
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getWaitTime().getSeconds()))
                .body(error);
    }
    
    @ExceptionHandler(TickerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTickerNotFound(TickerNotFoundException ex, WebRequest request) {
        logger.info("[{}] Ticker não encontrado: {}", getCorrelationId(), ex.getTicker());
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getErrorCode())
                .message("Ticker não encontrado.")
                .ticker(ex.getTicker())
                .correlationId(getCorrelationId())
                .timestamp(LocalDateTime.now())
                .retryable(ex.isRetryable())
                .details(Map.of(
                    "search_attempted", ex.getSearchAttempted(),
                    "suggestions", ex.getSimilarTickers(),
                    "has_suggestions", ex.hasSuggestions()
                ))
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(WebDriverInitializationException.class)
    public ResponseEntity<ErrorResponse> handleWebDriverInitialization(WebDriverInitializationException ex, WebRequest request) {
        logger.error("[{}] Falha na inicialização do WebDriver: driver={}, message={}", 
                    getCorrelationId(), ex.getDriverType(), ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getErrorCode())
                .message("Falha na inicialização do navegador. Tente novamente.")
                .correlationId(getCorrelationId())
                .timestamp(LocalDateTime.now())
                .retryable(ex.isRetryable())
                .details(Map.of(
                    "driver_type", ex.getDriverType(),
                    "error_category", "WEBDRIVER_INIT"
                ))
                .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
    
    @ExceptionHandler(NetworkCaptureException.class)
    public ResponseEntity<ErrorResponse> handleNetworkCapture(NetworkCaptureException ex, WebRequest request) {
        logger.warn("[{}] Falha na captura de rede: ticker={}, captured={}/{} URLs", 
                   getCorrelationId(), ex.getTicker(), ex.getCapturedUrls(), ex.getExpectedUrls());
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getErrorCode())
                .message("Falha na captura de dados de rede. Alguns dados podem estar incompletos.")
                .ticker(ex.getTicker())
                .correlationId(getCorrelationId())
                .timestamp(LocalDateTime.now())
                .retryable(ex.isRetryable())
                .details(Map.of(
                    "captured_urls", ex.getCapturedUrls(),
                    "expected_urls", ex.getExpectedUrls(),
                    "capture_rate", String.format("%.1f%%", (ex.getCapturedUrls() * 100.0) / ex.getExpectedUrls())
                ))
                .build();
        
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(error);
    }
    
    @ExceptionHandler(HtmlStructureException.class)
    public ResponseEntity<ErrorResponse> handleHtmlStructure(HtmlStructureException ex, WebRequest request) {
        logger.error("[{}] Estrutura HTML inválida: ticker={}, expected={}", 
                    getCorrelationId(), ex.getTicker(), ex.getExpectedElement());
        
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getErrorCode())
                .message("Estrutura da página alterada. A funcionalidade pode estar temporariamente indisponível.")
                .ticker(ex.getTicker())
                .correlationId(getCorrelationId())
                .timestamp(LocalDateTime.now())
                .retryable(ex.isRetryable())
                .details(Map.of(
                    "expected_element", ex.getExpectedElement() != null ? ex.getExpectedElement() : "unknown",
                    "actual_content", ex.getActualContent() != null ? ex.getActualContent() : "null",
                    "url", ex.getUrl() != null ? ex.getUrl() : "unknown"
                ))
                .build();
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("[{}] Erro não tratado: {}", getCorrelationId(), ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("Erro interno do servidor. Tente novamente mais tarde.")
                .correlationId(getCorrelationId())
                .timestamp(LocalDateTime.now())
                .retryable(true)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    private String getCorrelationId() {
        return CorrelationIdInterceptor.getCurrentCorrelationId();
    }
}