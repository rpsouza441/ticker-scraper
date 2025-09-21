package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.EtfResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper.EtfApiMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.EtfRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.EtfUseCasePort;
import com.github.dockerjava.api.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/etf")
public class EtfController {

    private static final Logger log = LoggerFactory.getLogger(EtfController.class);

    private final EtfUseCasePort useCase;      // retorna Domain (Etf)
    private final EtfApiMapper etfApiMapper;   // Domain -> ResponseDTO

    public EtfController(EtfUseCasePort useCase, EtfApiMapper etfApiMapper) {
        this.useCase = useCase;
        this.etfApiMapper = etfApiMapper;
    }

    @GetMapping("/get-{ticker}")
    public Mono<ResponseEntity<EtfResponseDTO>> get(@PathVariable String ticker) {
        log.info("GET /etf/get-{}", ticker);
        return useCase.getTickerData(ticker)          // Mono<Etf>
                .map(etfApiMapper::toResponseDto)         // Domain -> DTO
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/get-{ticker}/raw")
    public Mono<ResponseEntity<EtfRawDataResponse>> getRawData(@PathVariable String ticker) {
        log.info("GET /etf/get-{}/raw", ticker);
        return useCase.getRawTickerData(ticker)       // Mono<EtfRawDataResponse>
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }
}