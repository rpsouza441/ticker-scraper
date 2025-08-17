package br.dev.rodrigopinheiro.tickerscraper.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração do Spring MVC para customizações específicas.
 * Registra CorrelationIdInterceptor para cobertura MVC tradicional.
 * Funciona em conjunto com CorrelationIdWebFilter para WebFlux.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final CorrelationIdInterceptor correlationIdInterceptor;
    
    public WebMvcConfig(CorrelationIdInterceptor correlationIdInterceptor) {
        this.correlationIdInterceptor = correlationIdInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registrar interceptor de correlationId para todas as rotas MVC
        registry.addInterceptor(correlationIdInterceptor)
                .addPathPatterns("/**")  // Aplicar a todas as rotas
                .excludePathPatterns(
                    "/actuator/**",      // Excluir endpoints de monitoramento
                    "/error",            // Excluir página de erro
                    "/favicon.ico",      // Excluir favicon
                    "/static/**",        // Excluir recursos estáticos
                    "/css/**",           // Excluir CSS
                    "/js/**",            // Excluir JavaScript
                    "/images/**"         // Excluir imagens
                );
    }
    
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // Configurar timeout para requisições assíncronas (30 segundos)
        configurer.setDefaultTimeout(30000);
    }
}