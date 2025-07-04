package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.TickerDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.AcaoCardsScraper;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.AcaoDetailedInfoScraper;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.AcaoIndicatorsScraper;
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

@Component("seleniumScraper")
public class SeleniumScraperAdapter implements TickerDataScrapperPort {
    private static final Logger logger = LoggerFactory.getLogger(SeleniumScraperAdapter.class);
    private final HeaderScraper headerScraper;
    private final AcaoCardsScraper cardsScraper;
    private final AcaoDetailedInfoScraper detailedInfoScraper;
    private final AcaoIndicatorsScraper indicatorsScraper;

    public SeleniumScraperAdapter(HeaderScraper headerScraper,
                                  AcaoCardsScraper cardsScraper,
                                  AcaoDetailedInfoScraper detailedInfoScraper,
                                  AcaoIndicatorsScraper indicatorsScraper) {
        this.headerScraper = headerScraper;
        this.cardsScraper = cardsScraper;
        this.detailedInfoScraper = detailedInfoScraper;
        this.indicatorsScraper = indicatorsScraper;
    }

    @Override
    public Mono<AcaoDadosFinanceiros> scrape(String ticker) {
        String urlCompleta = "https://investidor10.com.br/acoes/" + ticker;
        logger.info("Iniciando scraping com Selenium para a url {}", urlCompleta);

        // O Mono.fromCallable é a ponte perfeita entre o mundo bloqueante (Selenium)
        // e o mundo reativo (WebFlux).
        return Mono.fromCallable(() -> {
            // --- A LÓGICA DO SELENIUM RODA AQUI ---

            // Requer que o chromedriver.exe esteja no seu PATH ou configurado via System.setProperty
            ChromeOptions options = new ChromeOptions();

            // Adicione o argumento para rodar em modo headless.
            // "--headless=new" é o novo padrão recomendado pelo time do Chrome.
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080"); // Ajuda a simular um navegador real

            // options.addArguments("--headless"); // Roda o navegador sem interface gráfica
            WebDriver driver = new ChromeDriver(options);

            String html;
            try {
                driver.get(urlCompleta);
                // Opcional: esperar um pouco para o JS carregar, se necessário
                // Thread.sleep(2000);
                html = driver.getPageSource(); // Pega o HTML FINAL, após a execução do JS
                logger.info("Selenium obteve HTML de {} caracteres.", html.length());
            } finally {
                driver.quit(); // Sempre feche o navegador!
            }

            // A partir daqui, o processo é o mesmo:
            Document doc = Jsoup.parse(html);
            InfoHeader header = headerScraper.scrapeInfoHeader(doc);
            AcaoInfoCards cards = cardsScraper.scrapeCardsInfo(doc);
            AcaoInfoDetailed detailed = detailedInfoScraper.scrapeAndParseDetailedInfo(doc);
            AcaoIndicadoresFundamentalistas indicators = indicatorsScraper.scrape(doc, ticker);

            return new AcaoDadosFinanceiros(header, detailed, cards, indicators);

        }).subscribeOn(Schedulers.boundedElastic()); // Executa toda essa operação bloqueante em uma thread segura
    }
}