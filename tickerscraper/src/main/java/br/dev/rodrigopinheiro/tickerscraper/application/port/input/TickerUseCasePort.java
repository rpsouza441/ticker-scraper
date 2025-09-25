package br.dev.rodrigopinheiro.tickerscraper.application.port.input;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.AtivoResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import reactor.core.publisher.Mono;

public interface TickerUseCasePort {
    
    /**
     * Obtém dados de qualquer tipo de ativo
     * @param ticker Código do ticker (ex: PETR3, SAPR11, AAPL34)
     * @return Dados unificados do ativo
     */
    Mono<AtivoResponseDTO> obterAtivo(String ticker);
    
    /**
     * Classifica tipo de ativo sem buscar dados
     * @param ticker Código do ticker
     * @return Tipo classificado
     */
    Mono<TipoAtivo> classificarTicker(String ticker);
}