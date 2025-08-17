package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.FiiResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper.FiiApiMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.FiiRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.FiiUseCasePort;
import com.github.dockerjava.api.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/fii")
public class FiiController {

    private static final Logger log = LoggerFactory.getLogger(FiiController.class);

    private final FiiUseCasePort useCase;     // retorna Domain (FundoImobiliario)
    private final FiiApiMapper fiiApiMapper;  // Domain -> ResponseDTO

    public FiiController(FiiUseCasePort useCase, FiiApiMapper fiiApiMapper) {
        this.useCase = useCase;
        this.fiiApiMapper = fiiApiMapper;
    }

    @GetMapping("/get-{ticker}")
    public Mono<ResponseEntity<FiiResponseDTO>> get(@PathVariable String ticker) {
        log.info("GET /fii/get-{}", ticker);
        return useCase.getTickerData(ticker)          // Mono<FundoImobiliario>
                .map(fiiApiMapper::toResponse)            // Domain -> DTO
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/get-{ticker}/raw")
    public Mono<ResponseEntity<FiiRawDataResponse>> getRaw(@PathVariable String ticker) {
        log.info("GET /fii/get-{}/raw", ticker);
        return useCase.getRawTickerData(ticker)       // Mono<FiiRawDataResponse>
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }
}

