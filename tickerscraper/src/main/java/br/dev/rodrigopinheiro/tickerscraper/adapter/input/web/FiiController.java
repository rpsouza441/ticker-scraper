package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.AcaoResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.FiiResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.FiiUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fii")
public class FiiController {
    private static final Logger logger = LoggerFactory.getLogger(FiiController.class);
    private final FiiUseCasePort service;

    public FiiController(FiiUseCasePort service) {
        this.service = service;
    }

    @GetMapping("/get-{ticker}")
    public Mono<ResponseEntity<FiiResponseDTO>> get(@PathVariable String ticker) {
        return service.getTickerData(ticker)
                .map(fiiEntity -> ResponseEntity.ok(new FiiResponseDTO()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Este é o endpoint que você quer testar!
    @GetMapping("/get-{ticker}/raw")
    public Mono<ResponseEntity<FiiDadosFinanceirosDTO>> getRawData(@PathVariable String ticker) {
        logger.info("Getting RAW FII data for ticker: {}", ticker);
        return service.getRawTickerData(ticker)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }




}
