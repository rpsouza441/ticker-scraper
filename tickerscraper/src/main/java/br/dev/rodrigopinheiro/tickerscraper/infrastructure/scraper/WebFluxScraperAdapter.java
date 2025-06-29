package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.TickerDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@Component
public class WebFluxScraperAdapter implements TickerDataScrapperPort {

    private final WebClient webClient;
    private final HeaderScraper headerScraper;
    private final CardsScraper cardsScraper;
    private final DetailedInfoScraper detailedInfoScraper;
    private final IndicatorsScraper indicatorsScraper;

    public WebFluxScraperAdapter(WebClient.Builder webClientBuilder, HeaderScraper headerScraper, CardsScraper cardsScraper, DetailedInfoScraper detailedInfoScraper, IndicatorsScraper indicatorsScraper) {
        this.webClient = webClientBuilder.baseUrl("https://investidor10.com.br/acoes/").build();
        this.headerScraper = headerScraper;
        this.cardsScraper = cardsScraper;
        this.detailedInfoScraper = detailedInfoScraper;
        this.indicatorsScraper = indicatorsScraper;
    }


    @Override
    public Mono<DadosFinanceiros> scrape(String ticker) throws IOException {
        // 1. FAZ A CHAMADA DE REDE NÃO-BLOQUEANTE
        return webClient.get()
                .uri(ticker) // A URL base já foi configurada
                .retrieve()
                .bodyToMono(String.class) // Retorna um Mono<String> com o HTML
                .flatMap(html -> Mono.fromCallable(() -> { // 2. Processa o resultado quando ele chegar

                    // 3. O PARSE DO JSOUP ACONTECE AQUI!
                    // Como Jsoup.parse é uma operação que pode consumir CPU,
                    // a colocamos em um Scheduler separado para não bloquear o event-loop.
                    Document doc = Jsoup.parse(html);

                    // 4. A orquestração dos seus scrapers existentes continua a mesma
                    InfoHeader header = headerScraper.scrapeInfoHeader(doc);
                    InfoCards cards = cardsScraper.scrapeCardsInfo(doc);
                    InfoDetailed detailed = detailedInfoScraper.scrapeAndParseDetailedInfo(doc);
                    IndicadoresFundamentalistas indicators = indicatorsScraper.scrape(doc, ticker);

                    return new DadosFinanceiros(header, detailed, cards, indicators);

                }).subscribeOn(Schedulers.boundedElastic())); // 5. Executa o parse em uma thread otimizada para tarefas bloqueantes/CPU

    }
}
