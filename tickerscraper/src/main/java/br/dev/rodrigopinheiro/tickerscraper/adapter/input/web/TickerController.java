package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.AtivoResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.ClassificacaoResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.TickerUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.TickerClassificationException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.TickerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
     * Endpoint unificado para qualquer tipo de ativo
     */
    @GetMapping("/{ticker}")
    public Mono<ResponseEntity<AtivoResponseDTO>> obterAtivo(@PathVariable String ticker) {
        log.info("Requisição recebida para ticker: {}", ticker);
        
        return tickerUseCase.obterAtivo(ticker)
            .map(ResponseEntity::ok)
            .onErrorResume(this::handleErrors)
            .doOnSuccess(response -> log.info("Resposta enviada para ticker {}: status={}", 
                ticker, response.getStatusCode()));
    }
    
    /**
     * Endpoint apenas para classificação (útil para debug)
     */
    @GetMapping("/{ticker}/classificacao")
    public Mono<ResponseEntity<ClassificacaoResponse>> classificarTicker(@PathVariable String ticker) {
        log.info("Requisição de classificação para ticker: {}", ticker);
        
        return tickerUseCase.classificarTicker(ticker)
            .map(tipo -> ResponseEntity.ok(new ClassificacaoResponse(ticker, tipo)))
            .onErrorResume(this::handleClassificationErrors);
    }
    
    /**
     * Tratamento de erros para busca de dados
     */
    private Mono<ResponseEntity<AtivoResponseDTO>> handleErrors(Throwable error) {
        log.error("Erro no endpoint de ticker: {}", error.getMessage(), error);
        
        if (error instanceof TickerNotFoundException) {
            return Mono.just(ResponseEntity.notFound().build());
        }
        
        if (error instanceof TickerClassificationException) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        
        if (error instanceof UnsupportedOperationException) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build());
        }
        
        return Mono.just(ResponseEntity.internalServerError().build());
    }
    
    /**
     * Tratamento de erros para classificação
     */
    private Mono<ResponseEntity<ClassificacaoResponse>> handleClassificationErrors(Throwable error) {
        log.error("Erro na classificação: {}", error.getMessage(), error);
        
        if (error instanceof TickerClassificationException) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        
        return Mono.just(ResponseEntity.internalServerError().build());
    }
    
    
}
