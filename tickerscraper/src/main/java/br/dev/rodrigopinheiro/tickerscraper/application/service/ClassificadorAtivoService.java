package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.ClassificadorAtivoPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Serviço de classificação híbrido que combina heurística e API externa.
 * 
 * Estratégia:
 * 1. 95% dos casos: Classificação heurística rápida
 * 2. 5% dos casos: Consulta API Brapi para casos ambíguos (final 11)
 * 
 * @author Rodrigo Pinheiro
 * @since 1.0
 */
@Service
public class ClassificadorAtivoService {
    
    private static final Logger log = LoggerFactory.getLogger(ClassificadorAtivoService.class);
    
    private final ClassificadorAtivoPort classificadorPort;
    
    // Estatísticas de uso
    private final AtomicInteger totalClassificacoes = new AtomicInteger(0);
    private final AtomicInteger classificacoesHeuristicas = new AtomicInteger(0);
    private final AtomicInteger classificacoesApi = new AtomicInteger(0);
    private final Map<TipoAtivo, AtomicInteger> contadorPorTipo = new ConcurrentHashMap<>();
    
    public ClassificadorAtivoService(ClassificadorAtivoPort classificadorPort) {
        this.classificadorPort = classificadorPort;
    }

    /**
     * Classifica um ativo usando estratégia híbrida.
     * 
     * @param ticker Código do ativo
     * @return Mono com o tipo classificado
     */
    public Mono<TipoAtivo> classificar(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            log.warn("Ticker inválido fornecido: {}", ticker);
            return Mono.just(TipoAtivo.DESCONHECIDO);
        }
        
        String normalizedTicker = ticker.trim().toUpperCase();
        totalClassificacoes.incrementAndGet();
        
        // Primeiro: tentar classificação heurística
        TipoAtivo tipoHeuristico = TipoAtivo.classificarPorHeuristica(normalizedTicker);
        
        if (!TipoAtivo.precisaConsultarApi(normalizedTicker)) {
            // 95% dos casos - classificação heurística é suficiente
            log.info("Ticker {} classificado por heurística como: {}", normalizedTicker, tipoHeuristico);
            classificacoesHeuristicas.incrementAndGet();
            incrementarContador(tipoHeuristico);
            return Mono.just(tipoHeuristico);
        }
        
        // 5% dos casos - precisa consultar API Brapi
        log.info("Ticker {} requer consulta à API Brapi (caso ambíguo)", normalizedTicker);
        
        return classificadorPort.classificarPorApi(normalizedTicker)
            .doOnSuccess(tipo -> {
                log.info("Ticker {} classificado via API Brapi como: {}", normalizedTicker, tipo);
                classificacoesApi.incrementAndGet();
                incrementarContador(tipo);
            })
            .doOnError(error -> {
                log.warn("Erro ao consultar API Brapi para ticker {} - usando heurística: {}", 
                        normalizedTicker, error.getMessage());
                classificacoesHeuristicas.incrementAndGet();
                incrementarContador(tipoHeuristico);
            })
            .onErrorReturn(tipoHeuristico);
    }

    /**
     * Verifica se um ticker precisa de consulta externa (API).
     * 
     * @param ticker código do ticker
     * @return true se precisa consultar API, false caso contrário
     */
    public boolean precisaConsultarApi(String ticker) {
        return TipoAtivo.precisaConsultarApi(ticker);
    }

    /**
     * Classifica múltiplos ativos em lote de forma assíncrona.
     * 
     * @param tickers Lista de códigos de ativos
     * @return Flux com os tipos classificados na mesma ordem
     */
    public Flux<TipoAtivo> classificarLote(List<String> tickers) {
        if (tickers == null || tickers.isEmpty()) {
            return Flux.empty();
        }
        
        log.info("Iniciando classificação em lote de {} tickers", tickers.size());
        
        return Flux.fromIterable(tickers)
            .flatMap(this::classificar, 5) // Máximo 5 consultas simultâneas
            .doOnComplete(() -> log.info("Classificação em lote concluída"));
    }
    
    /**
     * Incrementa o contador para um tipo específico.
     */
    private void incrementarContador(TipoAtivo tipo) {
        contadorPorTipo.computeIfAbsent(tipo, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    /**
     * Obtém estatísticas de uso do classificador.
     * 
     * @return Map com estatísticas detalhadas
     */
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        int total = totalClassificacoes.get();
        int heuristicas = classificacoesHeuristicas.get();
        int apis = classificacoesApi.get();
        
        stats.put("totalClassificacoes", total);
        stats.put("classificacoesHeuristicas", heuristicas);
        stats.put("classificacoesApi", apis);
        stats.put("percentualHeuristica", total > 0 ? (heuristicas * 100.0 / total) : 0.0);
        stats.put("percentualApi", total > 0 ? (apis * 100.0 / total) : 0.0);
        
        // Contadores por tipo
        Map<String, Integer> porTipo = new ConcurrentHashMap<>();
        contadorPorTipo.forEach((tipo, contador) -> 
            porTipo.put(tipo.name(), contador.get()));
        stats.put("classificacoesPorTipo", porTipo);
        
        return stats;
    }

}