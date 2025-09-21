package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.AtivoResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.ClassificacaoResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.TickerUseCasePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/ticker")
@RequiredArgsConstructor
public class TickerController {
    
    private final TickerUseCasePort tickerUseCase;
    
    /**
     * Endpoint unificado que detecta automaticamente o tipo de ativo
     */
    @GetMapping("/{ticker}")
    public Mono<ResponseEntity<AtivoResponseDTO>> obterDadosTicker(@PathVariable String ticker) {
        log.info("Requisição para ticker: {}", ticker);
        
        return tickerUseCase.obterAtivo(ticker)
            .map(ResponseEntity::ok);
    }

    /**
     * Endpoint apenas para classificação (útil para debug)
     */
    @GetMapping("/{ticker}/classificacao")
    public Mono<ResponseEntity<ClassificacaoResponse>> classificarTicker(@PathVariable String ticker) {
        log.info("Requisição de classificação para ticker: {}", ticker);
        
        return tickerUseCase.classificarTicker(ticker)
            .map(tipo -> ResponseEntity.ok(new ClassificacaoResponse(ticker, tipo)));
    }
    
    
}
