package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.AcaoResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper.AcaoApiMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.AcaoUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.service.AcaoUseCaseService;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoDadosFinanceirosDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/acao")
public class AcaoController {
    private static final Logger logger = LoggerFactory.getLogger(AcaoController.class);
    private final AcaoUseCasePort service;
    private final AcaoApiMapper acaoApiMapper;

    public AcaoController(AcaoUseCasePort service, AcaoApiMapper acaoApiMapper) {
        this.service = service;
        this.acaoApiMapper = acaoApiMapper;
    }

    @GetMapping("/get-{ticker}")
    public Mono<ResponseEntity<AcaoResponseDTO>> get(@PathVariable String ticker) {
        logger.info("Getting ticker: {}", ticker);
        // 1. O serviço é chamado e retorna o Mono<AcaoEntity>
        return service.getTickerData(ticker)
                // 2. Usamos .map() para transformar o resultado (quando ele chegar)
                .map(acaoEntity -> {
                    // O mapper converte a entidade para o DTO de resposta
                    logger.info("acaoEntity toString {}",acaoEntity.toString());
                    AcaoResponseDTO responseDto = acaoApiMapper.toResponseDto(acaoEntity);
                    logger.info("ResponseDTO toString {}",responseDto.toString());
                    // Retornamos um ResponseEntity com o DTO
                    return ResponseEntity.ok(responseDto);
                })
                // 3. Se o fluxo terminar vazio (ex: erro tratado), retorna um 404 Not Found
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint para retornar os dados brutos e completos raspados para um ticker.
     * Ideal para depuração ou consumidores que precisam de todos os campos.
     */
    @GetMapping("/get-{ticker}/raw")
    public Mono<ResponseEntity<AcaoDadosFinanceirosDTO>> getRawData(@PathVariable String ticker) {
        logger.info("Getting RAW data for ticker: {}", ticker);

        // 1. O serviço é chamado e retorna o Mono<DadosFinanceiros>
        return service.getRawTickerData(ticker)
                // 2. Usamos .map() para transformar o resultado (quando ele chegar) em uma resposta 200 OK
                .map(dadosFinanceiros -> ResponseEntity.ok(dadosFinanceiros))
                // 3. Se o fluxo terminar vazio (ex: erro não tratado), retorna um 404 Not Found
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


}
