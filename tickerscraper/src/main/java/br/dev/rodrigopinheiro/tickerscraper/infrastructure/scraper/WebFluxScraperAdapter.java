package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.TickerDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.AcaoCardsScraper;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.AcaoDetailedInfoScraper;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.AcaoIndicatorsScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@Component("webFluxScraper")
public class WebFluxScraperAdapter implements TickerDataScrapperPort {

    private static final Logger logger = LoggerFactory.getLogger(WebFluxScraperAdapter.class);


    private final WebClient webClient;
    private final HeaderScraper headerScraper;
    private final AcaoCardsScraper cardsScraper;
    private final AcaoDetailedInfoScraper detailedInfoScraper;
    private final AcaoIndicatorsScraper indicatorsScraper;

    public WebFluxScraperAdapter(WebClient webClient,
                                 HeaderScraper headerScraper,
                                 AcaoCardsScraper cardsScraper,
                                 AcaoDetailedInfoScraper detailedInfoScraper,
                                 AcaoIndicatorsScraper indicatorsScraper) {
        this.webClient = webClient;
        this.headerScraper = headerScraper;
        this.cardsScraper = cardsScraper;
        this.detailedInfoScraper = detailedInfoScraper;
        this.indicatorsScraper = indicatorsScraper;
    }

    public static final String URL = "https://investidor10.com.br/acoes/";

    @Override
    public Mono<AcaoDadosFinanceiros> scrape(String ticker) throws IOException {
        String urlCompleta = URL + ticker;
        logger.info("Iniciando requisição reativa para a url {}", urlCompleta);
        // 1. FAZ A CHAMADA DE REDE NÃO-BLOQUEANTE
        return webClient.get()
                .uri(urlCompleta)
                .headers(headers -> {
                    headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
                    headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                    headers.set("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7");
                    headers.set("Accept-Encoding", "gzip, deflate, br");
                    headers.set("Connection", "keep-alive");
                })// A URL base já foi configurada
                .retrieve()
                .bodyToMono(String.class) // Retorna um Mono<String> com o HTML

                // --- LOG PARA SUCESSO ---
                .doOnNext(html -> {
                    // Este bloco só é executado se a requisição for bem-sucedida e o corpo (HTML) for recebido.
                    // 'html' é a String contendo toda a página.
                    logger.info("WebClient obteve uma resposta para {}. Tamanho do HTML: {} caracteres.", urlCompleta, html.length());

                    // CUIDADO: Não logue o HTML inteiro em produção! Pode ter megabytes e poluir seus logs.
                    // Para depuração, logar os primeiros caracteres é uma ótima ideia.
                    logger.debug("Início do HTML recebido: {}", html.substring(0, Math.min(html.length(), 500)));
                })

                // --- LOG PARA ERRO ---
                .doOnError(error -> {
                    // Este bloco só é executado se a requisição do WebClient falhar ANTES do flatMap.
                    logger.error("Erro na chamada do WebClient para a URL {}: {}", urlCompleta, error.getMessage());
                })
                .flatMap(html -> Mono.fromCallable(() -> { // 2. Processa o resultado quando ele chegar
                    logger.debug("Iniciando parse do Jsoup na thread do Scheduler.");

                    // 3. O PARSE DO JSOUP ACONTECE AQUI!
                    // Como Jsoup.parse é uma operação que pode consumir CPU,
                    // a colocamos em um Scheduler separado para não bloquear o event-loop.
                    Document doc = Jsoup.parse(html);

                    // 4. A orquestração dos seus scrapers existentes continua a mesma
                    InfoHeader header = headerScraper.scrapeInfoHeader(doc);
                    AcaoInfoCards cards = cardsScraper.scrapeCardsInfo(doc);
                    AcaoInfoDetailed detailed = detailedInfoScraper.scrapeAndParseDetailedInfo(doc);
                    AcaoIndicadoresFundamentalistas indicators = indicatorsScraper.scrape(doc, ticker);
                    logger.info("Parse com Jsoup concluído com sucesso para {}.", ticker);

                    return new AcaoDadosFinanceiros(header, detailed, cards, indicators);

                }).subscribeOn(Schedulers.boundedElastic())); // 5. Executa o parse em uma thread otimizada para tarefas bloqueantes/CPU

    }
}
