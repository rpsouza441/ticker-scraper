package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper.BdrApiMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.BdrRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.BdrUseCasePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;



import br.dev.rodrigopinheiro.tickerscraper.domain.exception.TickerNotFoundException; // Importe sua exceção

@RestController
@RequestMapping("/bdr")
public class BdrController {

    private static final Logger log = LoggerFactory.getLogger(BdrController.class);

    private final BdrUseCasePort useCase;
    private final BdrApiMapper apiMapper;

    public BdrController(BdrUseCasePort useCase, BdrApiMapper apiMapper) {
        this.useCase = useCase;
        this.apiMapper = apiMapper;
    }

    @GetMapping("/get-{ticker}")
    public Mono<ResponseEntity<BdrResponseDTO>> get(@PathVariable String ticker) {
        log.info("GET /bdr/get-{}", ticker);
        return useCase.getTickerData(ticker)
                .map(apiMapper::toResponse)
                .map(ResponseEntity::ok)
                // CORREÇÃO: Use sua exceção de domínio
                .onErrorResume(TickerNotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/get-{ticker}/raw")
    public Mono<ResponseEntity<BdrRawDataResponse>> getRaw(@PathVariable String ticker) {
        log.info("GET /bdr/get-{}/raw", ticker);
        return useCase.getRawTickerData(ticker)
                .map(ResponseEntity::ok)
                // CORREÇÃO: Use sua exceção de domínio
                .onErrorResume(TickerNotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }
}