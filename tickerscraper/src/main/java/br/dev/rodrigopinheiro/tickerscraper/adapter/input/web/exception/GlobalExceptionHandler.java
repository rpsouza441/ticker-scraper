package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.exception.dto.ErrorResponse;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.AntiBotDetectedException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.AsyncRequestTimeoutException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.DataParsingException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.DomainException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.HtmlStructureException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.NetworkCaptureException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.RateLimitExceededException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingTimeoutException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.TickerClassificationException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.TickerNotFoundException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.WebDriverInitializationException;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.config.CorrelationIdInterceptor;

/**
 * Handler global para tratamento centralizado de exceções da aplicação.
 * Converte exceções de domínio em responses HTTP apropriadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex, WebRequest request) {
        HttpStatus status = switch (ex.getCode()) {
            case DOMAIN_VALIDATION, DIVIDEND_INVALID -> HttpStatus.UNPROCESSABLE_ENTITY; // 422
            case INVARIANT_VIOLATION -> HttpStatus.CONFLICT; // 409
            case TICKER_NOT_FOUND -> HttpStatus.NOT_FOUND; // 404
        };

        var error = ErrorResponse.builder()
                .code(ex.getCode().name()) // "DOMAIN_VALIDATION" etc.
                .message(ex.getMessage())
                .retryable(false)
                .timestamp(LocalDateTime.now())
                .correlationId(getCorrelationId())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ScrapingTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleScrapingTimeout(ScrapingTimeoutException ex, WebRequest request) {
        logger.warn("[{}] Timeout no scraping: ticker={}, url={}, timeout={}s",
                getCorrelationId(), ex.getTicker(), ex.getUrl(), ex.getTimeout().getSeconds());

        Map<String, Object> details = Map.of(
                "timeout_seconds", ex.getTimeout().getSeconds(),
                "operation", ex.getOperation());

        ErrorResponse error = createDomainErrorResponse(ex,
                "Timeout durante o scraping. Tente novamente em alguns minutos.", details);

        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(error);
    }

    @ExceptionHandler(AntiBotDetectedException.class)
    public ResponseEntity<ErrorResponse> handleAntiBotDetected(AntiBotDetectedException ex, WebRequest request) {
        logger.error("[{}] Anti-bot detectado: ticker={}, reason={}, method={}",
                getCorrelationId(), ex.getTicker(), ex.getDetectionReason(), ex.getDetectionMethod());

        Map<String, Object> details = Map.of(
                "detection_reason", ex.getDetectionReason(),
                "detection_method", ex.getDetectionMethod(),
                "try_alternative", ex.shouldTryAlternativeMethod());

        ErrorResponse error = createDomainErrorResponse(ex,
                "Acesso temporariamente bloqueado. Tente novamente mais tarde.", details);

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(DataParsingException.class)
    public ResponseEntity<ErrorResponse> handleDataParsing(DataParsingException ex, WebRequest request) {
        logger.error("[{}] Erro de parsing: ticker={}, selector={}, expected={}",
                getCorrelationId(), ex.getTicker(), ex.getSelector(), ex.getExpectedData(), ex);

        Map<String, Object> details = Map.of(
                "selector", ex.getSelector(),
                "expected_data", ex.getExpectedData(),
                "actual_content", ex.getActualContent() != null ? ex.getActualContent() : "null");

        ErrorResponse error = createDomainErrorResponse(ex,
                "Falha ao processar dados do site. A estrutura pode ter mudado.", details);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex, WebRequest request) {
        logger.warn("[{}] Rate limit excedido: ticker={}, requests={}, retry_after={}",
                getCorrelationId(), ex.getTicker(), ex.getRequestCount(), ex.getRetryAfter());

        Map<String, Object> details = Map.of(
                "request_count", ex.getRequestCount(),
                "retry_after", ex.getRetryAfter().toString(),
                "wait_seconds", ex.getWaitTime().getSeconds());
        ErrorResponse error = createDomainErrorResponse(ex,
                "Muitas requisições. Aguarde antes de tentar novamente.", details);

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getWaitTime().getSeconds()))
                .body(error);
    }

    @ExceptionHandler(TickerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTickerNotFound(TickerNotFoundException ex, WebRequest request) {
        logger.info("[{}] Ticker não encontrado: {}", getCorrelationId(), ex.getTicker());

        Map<String, Object> details = Map.of(
                "search_attempted", ex.getSearchAttempted(),
                "suggestions", ex.getSimilarTickers(),
                "has_suggestions", ex.hasSuggestions());
        ErrorResponse error = createDomainErrorResponse(ex,
                "Ticker não encontrado.", details);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(TickerClassificationException.class)
    public ResponseEntity<ErrorResponse> handleTickerClassification(TickerClassificationException ex,
            WebRequest request) {
        logger.warn("[{}] Erro na classificação de ticker: {}", getCorrelationId(), ex.getMessage());

        // Extrair ticker da mensagem de erro (formato: "Erro ao classificar ticker
        // 'TICKER': motivo")
        String ticker = extractTickerFromMessage(ex.getMessage());

        Map<String, Object> details = createDetailsMap(
                "CLASSIFICATION",
                "Verifique se o ticker está no formato correto (ex: PETR4, SAPR11)",
                Map.of());

        ErrorResponse error = createGenericErrorResponse(
                "TICKER_CLASSIFICATION_ERROR",
                "Erro ao classificar o ticker. Verifique se o código está correto.",
                ticker,
                details);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedOperation(UnsupportedOperationException ex,
            WebRequest request) {
        logger.info("[{}] Operação não suportada: {}", getCorrelationId(), ex.getMessage());

        // Tentar extrair ticker da mensagem se possível
        String ticker = extractTickerFromMessage(ex.getMessage());

        Map<String, Object> details = Map.of(
                "error_type", "UNSUPPORTED_OPERATION",
                "suggestion", "Este tipo de ativo ainda não é suportado pela API");

        ErrorResponse error = createGenericErrorResponse(
                "OPERATION_NOT_SUPPORTED",
                "Operação não suportada para este tipo de ativo.",
                ticker,
                details);

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(error);
    }

    @ExceptionHandler(WebDriverInitializationException.class)
    public ResponseEntity<ErrorResponse> handleWebDriverInitialization(WebDriverInitializationException ex,
            WebRequest request) {
        logger.error("[{}] Falha na inicialização do WebDriver: driver={}, message={}",
                getCorrelationId(), ex.getDriverType(), ex.getMessage());

        Map<String, Object> details = Map.of(
                "driver_type", ex.getDriverType(),
                "error_category", "WEBDRIVER_INIT");
        ErrorResponse error = createDomainErrorResponse(ex,
                "Falha na inicialização do navegador. Tente novamente.", details);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(NetworkCaptureException.class)
    public ResponseEntity<ErrorResponse> handleNetworkCapture(NetworkCaptureException ex, WebRequest request) {
        logger.warn("[{}] Falha na captura de rede: ticker={}, captured={}/{} URLs",
                getCorrelationId(), ex.getTicker(), ex.getCapturedUrls(), ex.getExpectedUrls());

        Map<String, Object> details = Map.of(
                "captured_urls", ex.getCapturedUrls(),
                "expected_urls", ex.getExpectedUrls(),
                "capture_rate", String.format("%.1f%%", (ex.getCapturedUrls() * 100.0) / ex.getExpectedUrls()));
        ErrorResponse error = createDomainErrorResponse(ex,
                "Falha na captura de dados de rede. Alguns dados podem estar incompletos.", details);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(error);
    }

    @ExceptionHandler(HtmlStructureException.class)
    public ResponseEntity<ErrorResponse> handleHtmlStructure(HtmlStructureException ex, WebRequest request) {
        logger.error("[{}] Estrutura HTML inválida: ticker={}, expected={}",
                getCorrelationId(), ex.getTicker(), ex.getExpectedElement());

        Map<String, Object> details = Map.of(
                "expected_element", ex.getExpectedElement() != null ? ex.getExpectedElement() : "unknown",
                "actual_content", ex.getActualContent() != null ? ex.getActualContent() : "null",
                "url", ex.getUrl() != null ? ex.getUrl() : "unknown");
        ErrorResponse error = createDomainErrorResponse(ex,
                "Estrutura da página alterada. A funcionalidade pode estar temporariamente indisponível.", details);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleAsyncRequestTimeout(AsyncRequestTimeoutException ex,
            WebRequest request) {
        logger.warn("[{}] Timeout em operação assíncrona: ticker={}, operation={}, timeout={}s, requestId={}",
                getCorrelationId(), ex.getTicker(), ex.getOperation(), ex.getTimeout().getSeconds(), ex.getRequestId());

        Map<String, Object> details = Map.of(
                "operation", ex.getOperation(),
                "timeout_seconds", ex.getTimeout().getSeconds(),
                "request_id", ex.getRequestId() != null ? ex.getRequestId() : "unknown",
                "recommended_retry_delay_seconds", ex.getRecommendedRetryDelay().getSeconds());
        ErrorResponse error = createDomainErrorResponse(ex,
                "Operação assíncrona excedeu o tempo limite. Tente novamente em alguns minutos.", details);

        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .header("Retry-After", String.valueOf(ex.getRecommendedRetryDelay().getSeconds()))
                .body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, WebRequest request) {
        logger.debug("[{}] Recurso não encontrado: {}", getCorrelationId(), ex.getResourcePath());

        ErrorResponse error = createBaseErrorResponse(
                "RESOURCE_NOT_FOUND",
                "Recurso não encontrado: " + ex.getResourcePath(),
                null,
                false).build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("[{}] Erro não tratado: {}", getCorrelationId(), ex.getMessage(), ex);

        String ticker = extractTickerFromMessage(ex.getMessage());
        Map<String, Object> details = createDetailsMap(
                "GENERIC_ERROR",
                "Verifique os logs para mais detalhes",
                Map.of("exceptionType", ex.getClass().getSimpleName()));

        ErrorResponse error = createGenericErrorResponse(
                "INTERNAL_ERROR",
                "Erro interno do servidor. Tente novamente mais tarde.",
                ticker,
                details);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private String getCorrelationId() {
        return CorrelationIdInterceptor.getCurrentCorrelationId();
    }

    /**
     * Extrai ticker de mensagens de erro com múltiplos padrões
     */
    private String extractTickerFromMessage(String message) {
        if (message == null)
            return null;

        // Padrão 1: ticker 'TICKER'
        Pattern pattern1 = Pattern.compile("ticker '([^']+)'");
        Matcher matcher1 = pattern1.matcher(message);
        if (matcher1.find()) {
            return matcher1.group(1);
        }

        // Padrão 2: ticker: TICKER
        Pattern pattern2 = Pattern.compile("ticker:?\\s+([A-Z0-9]{4,6})");
        Matcher matcher2 = pattern2.matcher(message);
        if (matcher2.find()) {
            return matcher2.group(1);
        }

        // Padrão 3: TICKER no início da mensagem
        Pattern pattern3 = Pattern.compile("^([A-Z0-9]{4,6})\\s");
        Matcher matcher3 = pattern3.matcher(message);
        if (matcher3.find()) {
            return matcher3.group(1);
        }

        return null;
    }

    /**
     * Cria ErrorResponse base para exceções de domínio que estendem
     * ScrapingException
     */
    private ErrorResponse.Builder createBaseErrorResponse(
            String code, String message, String ticker, boolean retryable) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .ticker(ticker)
                .correlationId(getCorrelationId())
                .timestamp(LocalDateTime.now())
                .retryable(retryable);
    }

    /**
     * Cria ErrorResponse para exceções de domínio específicas
     */
    private ErrorResponse createDomainErrorResponse(
            ScrapingException ex, String message, Map<String, Object> details) {
        return createBaseErrorResponse(ex.getErrorCode(), message, ex.getTicker(), ex.isRetryable())
                .details(details)
                .build();
    }

    /**
     * Cria ErrorResponse para exceções genéricas
     */
    private ErrorResponse createGenericErrorResponse(
            String code, String message, String ticker, Map<String, Object> details) {
        return createBaseErrorResponse(code, message, ticker, true)
                .details(details)
                .build();
    }

    /**
     * Cria map de detalhes com campos base + específicos
     */
    private Map<String, Object> createDetailsMap(String errorType, String suggestion, Map<String, Object> specific) {
        Map<String, Object> details = new HashMap<>();
        if (errorType != null)
            details.put("error_type", errorType);
        if (suggestion != null)
            details.put("suggestion", suggestion);
        if (specific != null)
            details.putAll(specific);
        return details;
    }

}