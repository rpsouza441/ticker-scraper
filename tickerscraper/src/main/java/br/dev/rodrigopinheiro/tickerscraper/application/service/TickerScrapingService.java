package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.TickerDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.DadosFinanceiros;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;


@Service
public class TickerScrapingService {
    private final TickerDataScrapperPort scraper;

    public TickerScrapingService(@Qualifier("seleniumScraper")TickerDataScrapperPort scraper) {
        this.scraper = scraper;
    }

    public DadosFinanceiros getTickerData(String ticker) throws IOException {
        // 1. A porta te retorna uma "promessa"
        Mono<DadosFinanceiros> dadosMono = scraper.scrape(ticker);

        try {
            // 2. O .block() é a "ponte". Ele PAUSA a execução aqui,
            // espera o Mono completar, e "desembrulha" o objeto DadosFinanceiros.
            // Opcionalmente, você pode definir um tempo máximo de espera.
            return dadosMono.block(Duration.ofSeconds(30)); // Espera por até 30 segundos

        } catch (Exception e) {
            // TODO TRATAMENTO DE ERRO
            // Aquium  tratar o erro de forma elegante.
            // Lançar uma exceção customizada, logar o erro, retornar um objeto vazio, etc.
            System.err.println("Falha ao buscar dados para o ticker " + ticker + ": " + e.getMessage());
            // throw new TickerNotFoundException("Não foi possível encontrar dados para o ticker: " + ticker, e);
            return null; // ou retornar um objeto vazio, dependendo da sua regra de negócio
        }
    }
}
