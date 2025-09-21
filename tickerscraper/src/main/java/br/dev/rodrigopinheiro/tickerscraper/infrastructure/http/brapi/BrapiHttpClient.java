package br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi;

import br.dev.rodrigopinheiro.tickerscraper.domain.exception.NetworkCaptureException;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResponse;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente HTTP para integração com a API Brapi.
 * 
 * Implementa retry com backoff exponencial e jitter para resiliência.
 * 
 * @author Rodrigo Pinheiro
 * @since 1.0
 */
@Component
public class BrapiHttpClient {
    
    private static final Logger log = LoggerFactory.getLogger(BrapiHttpClient.class);
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String token;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final int maxAttempts;
    private final Duration initialDelay;
    private final double multiplier;
    private final Duration maxDelay;
    private final double jitter;
    
    public BrapiHttpClient(
            @Value("${brapi.base-url:https://brapi.dev/api}") String baseUrl,
            @Value("${brapi.token:}") String token,
            @Value("${brapi.connect-timeout:2s}") Duration connectTimeout,
            @Value("${brapi.read-timeout:2s}") Duration readTimeout,
            @Value("${brapi.retry.max-attempts:3}") int maxAttempts,
            @Value("${brapi.retry.initial-delay:200ms}") Duration initialDelay,
            @Value("${brapi.retry.multiplier:2.0}") double multiplier,
            @Value("${brapi.retry.max-delay:800ms}") Duration maxDelay,
            @Value("${brapi.retry.jitter:0.1}") double jitter,
            ObjectMapper objectMapper) {
        
        this.baseUrl = baseUrl;
        this.token = token;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.maxAttempts = maxAttempts;
        this.initialDelay = initialDelay;
        this.multiplier = multiplier;
        this.maxDelay = maxDelay;
        this.jitter = jitter;
        this.objectMapper = objectMapper;
        
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .build();
        
        log.info("BrapiHttpClient inicializado - baseUrl: {}, timeout: {}s, retry: {} tentativas", 
                baseUrl, connectTimeout.getSeconds(), maxAttempts);
    }
    
    /**
     * Consulta informações de um ticker na API Brapi.
     * 
     * @param ticker Código do ativo (ex: PETR4, HGLG11)
     * @return Mono com a resposta da API
     */
    public Mono<BrapiQuoteResponse> getQuote(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Ticker não pode ser nulo ou vazio"));
        }
        
        String normalizedTicker = ticker.trim().toUpperCase();
        log.debug("Consultando ticker {} na API Brapi", normalizedTicker);
        
        return Mono.fromCallable(() -> executeRequest(normalizedTicker))
            .retryWhen(createRetrySpec())
            .doOnSuccess(response -> log.debug("Resposta recebida para ticker {}: {} resultados", 
                    normalizedTicker, response.hasResults() ? response.results().size() : 0))
            .doOnError(error -> log.error("Erro ao consultar ticker {} na API Brapi", 
                    normalizedTicker, error));
    }
    
    private BrapiQuoteResponse executeRequest(String ticker) throws Exception {
        String url = buildUrl(ticker);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(readTimeout)
            .header("Accept", "application/json")
            .header("User-Agent", "TickerScraper/1.0")
            .GET()
            .build();
        
        log.trace("Executando request: {}", url.replaceAll("token=[^&]*", "token=***"));
        
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new NetworkCaptureException(ticker,
                String.format("API Brapi retornou status %d", response.statusCode()));
        }
        
        String responseBody = response.body();
        
        // Verificar se é uma resposta de erro da API
        if (responseBody.contains("\"error\":true")) {
            try {
                BrapiErrorResponse errorResponse = objectMapper.readValue(responseBody, BrapiErrorResponse.class);
                if (errorResponse.isTickerNotFound()) {
                    throw new NetworkCaptureException(ticker, 
                        String.format("API Brapi retornou erro: %s", errorResponse.message()));
                }
            } catch (Exception e) {
                log.warn("Erro ao parsear resposta de erro da API Brapi: {}", e.getMessage());
            }
        }
        
        return objectMapper.readValue(responseBody, BrapiQuoteResponse.class);
    }
    
    private String buildUrl(String ticker) {
        StringBuilder url = new StringBuilder(baseUrl)
            .append("/quote/")
            .append(ticker);
        
        if (token != null && !token.trim().isEmpty()) {
            url.append("?token=").append(token);
        }
        
        return url.toString();
    }
    
    private Retry createRetrySpec() {
        return Retry.backoff(maxAttempts, initialDelay)
            .maxBackoff(maxDelay)
            .multiplier(multiplier)
            .jitter(jitter)
            .filter(this::isRetryableException)
            .doBeforeRetry(retrySignal -> 
                log.warn("Tentativa {} de {} para API Brapi - erro: {}", 
                    retrySignal.totalRetries() + 1, maxAttempts, 
                    retrySignal.failure().getMessage()));
    }
    
    private boolean isRetryableException(Throwable throwable) {
        // Retry em casos de timeout, conexão ou erros 5xx
        return throwable instanceof java.net.SocketTimeoutException ||
               throwable instanceof java.net.ConnectException ||
               throwable instanceof java.io.IOException ||
               (throwable instanceof NetworkCaptureException && 
                throwable.getMessage().contains("status 5"));
    }
}