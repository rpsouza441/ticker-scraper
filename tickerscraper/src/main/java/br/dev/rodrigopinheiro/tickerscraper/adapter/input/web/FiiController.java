package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.FiiResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper.FiiApiMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.FiiUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fii")
public class FiiController {
    private static final Logger logger = LoggerFactory.getLogger(FiiController.class);

    private final FiiUseCasePort service;
    private final FiiApiMapper fiiApiMapper;

    public FiiController(FiiUseCasePort service, FiiApiMapper fiiApiMapper) {
        this.service = service;
        this.fiiApiMapper = fiiApiMapper;
    }

    @GetMapping("/get-{ticker}")
    public Mono<ResponseEntity<FiiResponseDTO>> get(@PathVariable String ticker) {
        final String t = normalize(ticker);
        try (var ignored = MDC.putCloseable("ticker", t)) {
            logger.info("Getting FII {}", t);
            return service.getTickerData(t)
                    .map(entity -> ResponseEntity.ok(fiiApiMapper.toResponseDto(entity)))
                    .defaultIfEmpty(ResponseEntity.notFound().build())
                    .onErrorResume(e -> {
                        logger.error("Erro em GET /fii/get-{}: {}", t, e.toString(), e);
                        return Mono.just(ResponseEntity.internalServerError().build());
                    });
        }
    }

    /** RAW: leitura — não salva se cache estiver inválido */
    @GetMapping("/get-{ticker}/raw")
    public Mono<ResponseEntity<FiiDadosFinanceirosDTO>> getRaw(@PathVariable String ticker) {
        final String t = normalize(ticker);
        try (var ignored = MDC.putCloseable("ticker", t)) {
            logger.info("Getting RAW FII {}", t);
            return service.getRawTickerData(t)
                    .map(ResponseEntity::ok)
                    .defaultIfEmpty(ResponseEntity.notFound().build())
                    .onErrorResume(e -> {
                        logger.error("Erro em GET /fii/get-{}/raw: {}", t, e.toString(), e);
                        return Mono.just(ResponseEntity.internalServerError().build());
                    });
        }
    }


    private static String normalize(String ticker) {
        return ticker == null ? null : ticker.trim().toUpperCase();
    }
}
