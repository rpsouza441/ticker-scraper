package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import reactor.core.publisher.Mono;

/**
 * Port de saída para classificação de ativos via API externa.
 * 
 * Define o contrato para classificação de ativos quando a heurística
 * não é suficiente (ex: tickers terminados em 11).
 * 
 * @author Rodrigo Pinheiro
 * @since 1.0
 */
public interface ClassificadorAtivoPort {
    
    /**
     * Classifica um ativo consultando API externa.
     * 
     * @param ticker Código do ativo (ex: HGLG11, XPML11)
     * @return Mono com o tipo do ativo ou DESCONHECIDO se não encontrado
     */
    Mono<TipoAtivo> classificarPorApi(String ticker);
    
    /**
     * Verifica se a API externa está disponível.
     * 
     * @return Mono com true se disponível, false caso contrário
     */
    Mono<Boolean> isApiDisponivel();
}