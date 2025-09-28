package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.BdrRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.BdrUseCasePort;
import com.github.dockerjava.api.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bdr")
public class BdrController {

    private static final Logger log = LoggerFactory.getLogger(BdrController.class);

    private final BdrUseCasePort useCase;

    public BdrController(BdrUseCasePort useCase) {
        this.useCase = useCase;
    }

    // 200 OK vazio (não chama service/DB)
    @GetMapping("/get-{ticker}")
    public Mono<ResponseEntity<Void>> get(@PathVariable String ticker) {
        log.info("GET /bdr/get-{}", ticker);
        return Mono.just(ResponseEntity.status(HttpStatus.OK).<Void>build());
    }


    @GetMapping("/get-{ticker}/raw")
    public Mono<ResponseEntity<BdrRawDataResponse>> getRaw(@PathVariable String ticker) {
        log.info("GET /bdr/get-{}/raw", ticker);
        return useCase.getRawTickerData(ticker)
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }
}
