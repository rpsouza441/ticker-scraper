package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.application.service.TickerScrapingService;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.DadosFinanceiros;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/acao")
public class AcaoController {
    private static final Logger logger = LoggerFactory.getLogger(AcaoController.class);
    private final TickerScrapingService tickerScrapingService;

    public AcaoController(TickerScrapingService tickerScrapingService) {
        this.tickerScrapingService = tickerScrapingService;
    }

    @GetMapping("/get-{ticker}")
    public ResponseEntity<DadosFinanceiros> get(@PathVariable String ticker) throws IOException {
        logger.info("Getting ticker: {}", ticker);
        DadosFinanceiros tickerData = tickerScrapingService.getTickerData(ticker);
        return ResponseEntity.status(HttpStatus.OK).body(tickerData);
    }
}
