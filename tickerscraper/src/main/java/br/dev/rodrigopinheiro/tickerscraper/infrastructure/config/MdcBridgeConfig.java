package br.dev.rodrigopinheiro.tickerscraper.infrastructure.config;

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração para integrar MDC (Mapped Diagnostic Context) com Reactor Context.
 * Registra um ThreadLocalAccessor que permite propagação automática do MDC
 * entre threads reativas usando o mecanismo do Micrometer Context Propagation.
 */
@Configuration
public class MdcBridgeConfig {

    /**
     * Registra o accessor do MDC no ContextRegistry do Micrometer.
     * Isso permite que o correlationId e outros dados do MDC sejam
     * automaticamente propagados para threads reativas.
     */
    @PostConstruct
    void setup() {
        ContextRegistry.getInstance()
            .registerThreadLocalAccessor(
                "mdc",                       // nome lógico
                MDC::getCopyOfContextMap,    // captura o MDC atual
                ctx -> {                     // restaura (ou limpa)
                    if (ctx == null) {
                        MDC.clear();
                    } else {
                        MDC.setContextMap(ctx);
                    }
                },
                MDC::clear                   // clear quando ausente no Context
            );
    }
}