package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper.BdrApiMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.BdrRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.BdrUseCasePort;
import com.github.dockerjava.api.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bdr")
public class BdrController {

    private static final Logger log = LoggerFactory.getLogger(BdrController.class);

    private final BdrUseCasePort useCase;
    private final BdrApiMapper mapper;

    public BdrController(BdrUseCasePort useCase, BdrApiMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @GetMapping("/get-{ticker}")
    public Mono<ResponseEntity<BdrResponseDTO>> get(@PathVariable String ticker) {
        log.info("GET /bdr/get-{}", ticker);
        return useCase.getTickerData(ticker)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class,
                        error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/get-{ticker}/raw")
    public Mono<ResponseEntity<BdrRawDataResponse>> getRaw(@PathVariable String ticker) {
        log.info("GET /bdr/get-{}/raw", ticker);
        return useCase.getRawTickerData(ticker)
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class,
                        error -> Mono.just(ResponseEntity.notFound().build()));
    }
}
