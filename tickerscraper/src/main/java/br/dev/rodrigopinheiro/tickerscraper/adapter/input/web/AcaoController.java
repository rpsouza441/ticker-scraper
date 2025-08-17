package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.AcaoResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper.AcaoApiMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.AcaoRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.AcaoUseCasePort;
import com.github.dockerjava.api.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/acao")
public class AcaoController {

    private static final Logger log = LoggerFactory.getLogger(AcaoController.class);

    private final AcaoUseCasePort useCase;      // retorna Domain (Acao)
    private final AcaoApiMapper acaoApiMapper;  // Domain -> ResponseDTO

    public AcaoController(AcaoUseCasePort useCase, AcaoApiMapper acaoApiMapper) {
        this.useCase = useCase;
        this.acaoApiMapper = acaoApiMapper;
    }

    @GetMapping("/get-{ticker}")
    public Mono<ResponseEntity<AcaoResponseDTO>> get(@PathVariable String ticker) {
        log.info("GET /acao/get-{}", ticker);
        return useCase.getTickerData(ticker)          // Mono<Acao>
                .map(acaoApiMapper::toResponseDto)        // Domain -> DTO
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/get-{ticker}/raw")
    public Mono<ResponseEntity<AcaoRawDataResponse>> getRawData(@PathVariable String ticker) {
        log.info("GET /acao/get-{}/raw", ticker);
        return useCase.getRawTickerData(ticker)       // Mono<AcaoRawDataResponse>
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }
}
