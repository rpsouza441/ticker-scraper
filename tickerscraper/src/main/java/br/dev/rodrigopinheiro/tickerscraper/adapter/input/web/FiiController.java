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
        logger.info("Getting FII data for ticker: {}", ticker);
        return service.getTickerData(ticker)
                .map(fiiEntity -> {
                    // No futuro, você usará um mapper aqui:
                    // FiiResponseDTO responseDto = fiiApiMapper.toResponseDto(fiiEntity);
                    FiiResponseDTO responseDto = new FiiResponseDTO(); // Placeholder
                    return ResponseEntity.ok(responseDto);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    /**
     * Endpoint para retornar os dados brutos e completos raspados para um FII.
     * Ideal para depuração e para visualizar o progresso do scraper.
     */
    @GetMapping("/get-{ticker}/raw")
    public Mono<ResponseEntity<FiiDadosFinanceirosDTO>> getRawData(@PathVariable String ticker) {
        logger.info("Getting RAW FII data for ticker: {}", ticker);
        return service.getRawTickerData(ticker)
                .map(dadosBrutos -> ResponseEntity.ok(dadosBrutos))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }



}
