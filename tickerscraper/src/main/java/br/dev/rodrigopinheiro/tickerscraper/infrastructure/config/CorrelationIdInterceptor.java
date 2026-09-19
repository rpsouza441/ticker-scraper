package br.dev.rodrigopinheiro.tickerscraper.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.util.UUID;

/**
 * Interceptor para gerenciar correlationId em requisições HTTP tradicionais (MVC).
 * Funciona em conjunto com CorrelationIdWebFilter para cobertura completa.
 * Garante rastreabilidade de requisições através de todos os componentes do sistema.
 */
@Component
public class CorrelationIdInterceptor implements AsyncHandlerInterceptor {
    
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Obter correlationId do header ou gerar novo
        String correlationId = (String) request.getAttribute(CORRELATION_ID_MDC_KEY);
        if (correlationId == null) correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = generateCorrelationId();
        }
        request.setAttribute(CORRELATION_ID_MDC_KEY, correlationId);
        
        // Adicionar ao MDC para logs
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        MDC.put(REQUEST_ID_MDC_KEY, correlationId);
        
        // Adicionar informações de contexto da requisição
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        MDC.put("userAgent", request.getHeader("User-Agent"));
        MDC.put("remoteAddr", getClientIpAddress(request));
        
        // Retornar correlationId no response header
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        Object variables = request.getAttribute(org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (variables instanceof java.util.Map<?, ?> map && map.get("ticker") instanceof String ticker
                && !ticker.matches("(?i)[A-Z]{4}[0-9]{1,2}")) {
            throw new IllegalArgumentException("Invalid ticker format");
        }
        return true;
    }
    
    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response,
                                               Object handler) {
        MDC.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        // Limpar MDC após processamento da requisição
        MDC.clear();
    }
    
    /**
     * Gera um correlationId único baseado em UUID.
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
    
    /**
     * Obtém o IP real do cliente considerando proxies e load balancers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Pegar o primeiro IP da lista (cliente original)
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Obtém o correlationId atual do MDC.
     */
    public static String getCurrentCorrelationId() {
        return MDC.get(CORRELATION_ID_MDC_KEY);
    }
    
    /**
     * Define um correlationId específico (útil para testes ou processamento assíncrono).
     */
    public static void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        MDC.put(REQUEST_ID_MDC_KEY, correlationId);
    }
    
    /**
     * Remove o correlationId do contexto atual.
     */
    public static void clearCorrelationId() {
        MDC.remove(CORRELATION_ID_MDC_KEY);
        MDC.remove(REQUEST_ID_MDC_KEY);
    }
}
