package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component("acaoSeleniumScraper")
public class AcaoSeleniumScraperAdapter implements AcaoDataScrapperPort {
    private static final Logger logger = LoggerFactory.getLogger(AcaoSeleniumScraperAdapter.class);
    private final AcaoHeaderScraper acaoHeaderScraper;
    private final AcaoCardsScraper cardsScraper;
    private final AcaoDetailedInfoScraper detailedInfoScraper;
    private final AcaoIndicatorsScraper indicatorsScraper;

    public AcaoSeleniumScraperAdapter(AcaoHeaderScraper acaoHeaderScraper,
                                      AcaoCardsScraper cardsScraper,
                                      AcaoDetailedInfoScraper detailedInfoScraper,
                                      AcaoIndicatorsScraper indicatorsScraper) {
        this.acaoHeaderScraper = acaoHeaderScraper;
        this.cardsScraper = cardsScraper;
        this.detailedInfoScraper = detailedInfoScraper;
        this.indicatorsScraper = indicatorsScraper;
    }

    @Override
    public Mono<AcaoDadosFinanceirosDTO> scrape(String ticker) {
        String urlCompleta = "https://investidor10.com.br/acoes/" + ticker;
        logger.info("Iniciando scraping com Selenium para a url {}", urlCompleta);

        // 1. Criamos um Mono que representa a "missão" de ter um WebDriver pronto.
        Mono<WebDriver> driverMono = Mono.fromCallable(() -> {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            return new ChromeDriver(options);
        });

        return driverMono.flatMap(driver -> {
                    // 2. flatMap: Quando o driver estiver pronto, execute a lógica de scraping.
                    return Mono.fromCallable(() -> {
                                // O código de scraping em si é o mesmo...
                                driver.get(urlCompleta);
                                String html = driver.getPageSource();
                                logger.info("Selenium obteve HTML de {} caracteres.", html.length());
                                Document doc = Jsoup.parse(html);

                                AcaoInfoHeaderDTO header = acaoHeaderScraper.scrapeInfoHeader(doc);
                                AcaoInfoCardsDTO cards = cardsScraper.scrapeCardsInfo(doc);
                                AcaoInfoDetailedDTO detailed = detailedInfoScraper.scrapeAndParseDetailedInfo(doc);
                                AcaoIndicadoresFundamentalistasDTO indicators = indicatorsScraper.scrape(doc, ticker);
                                AcaoDadosFinanceirosDTO dadosFinanceiros = new AcaoDadosFinanceirosDTO(header, detailed, cards, indicators);
                                logger.info("Dados financeiros de Acao {}", dadosFinanceiros.toString());
                                return dadosFinanceiros;

                            })// 3. doFinally: Garante que, aconteça o que acontecer (sucesso ou erro),
                            // a limpeza será executada. É o equivalente reativo do `finally`.
                            .doFinally(signalType -> {
                                logger.info("Finalizando a sessão do Selenium para {}. Sinal: {}", ticker, signalType);
                                driver.quit();
                            });
                })
                // 4. doOnError: Adiciona um "espião" para logar qualquer erro que ocorra no fluxo.
                .doOnError(error -> logger.error("O Mono do AcaoSeleniumScraperAdapter falhou para o ticker {}", ticker, error))
                // 5. subscribeOn: Garante que TODA a operação (desde criar o driver até o final)
                // rode em uma thread segura.
                .subscribeOn(Schedulers.boundedElastic());


    }
}