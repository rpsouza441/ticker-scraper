package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.PlaywrightInitializer;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base.AbstractScraperAdapter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiCotacaoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDividendoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiIndicadorHistoricoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoCardsDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoHeaderDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoSobreDTO;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.FiiApiConstants.*;

@Component("fiiPlaywrightDirectScraper")
public class FiiPlaywrightDirectScraperAdapter extends AbstractScraperAdapter<FiiDadosFinanceirosDTO> implements FiiDataScrapperPort {

    private static final Logger logger = LoggerFactory.getLogger(FiiPlaywrightDirectScraperAdapter.class);
    
    // Constantes para seletores CSS com fallbacks para validação de elementos essenciais
    private static final String[] ESSENTIAL_SELECTORS = {"div.name-ticker", "div.container-header", "header div.fii-info"};
    private static final String[] CARDS_SELECTORS = {"section#cards-ticker", ".cards-section", ".fii-cards"};
    private static final String[] ABOUT_SELECTORS = {"div#about-company", "div.about-section", ".fii-about"};

    private final PlaywrightInitializer pwInit;                 // Browser singleton (abre/fecha no @PostConstruct/@PreDestroy)
    private final FiiSeleniumScraperAdapter seleniumFallback;  // Fallback quando Playwright falhar
    private final FiiHeaderScraper headerScraper;
    private final FiiInfoSobreScraper infoSobreScraper;
    private final FiiCardsScraper cardsScraper;
    private final FiiInternalIdScrapper internalIdScrapper;
    private final FiiApiScraper apiScraper;

    public FiiPlaywrightDirectScraperAdapter(
            PlaywrightInitializer pwInit,
            FiiSeleniumScraperAdapter seleniumFallback,
            FiiHeaderScraper headerScraper,
            FiiInfoSobreScraper infoSobreScraper,
            FiiCardsScraper cardsScraper,
            FiiInternalIdScrapper internalIdScrapper,
            FiiApiScraper apiScraper
    ) {
        this.pwInit = pwInit;
        this.seleniumFallback = seleniumFallback;
        this.headerScraper = headerScraper;
        this.infoSobreScraper = infoSobreScraper;
        this.cardsScraper = cardsScraper;
        this.internalIdScrapper = internalIdScrapper;
        this.apiScraper = apiScraper;
    }

    @Override
    @CircuitBreaker(name = "scraper", fallbackMethod = "fallbackToSelenium")
    @Retry(name = "scraper")
    public Mono<FiiDadosFinanceirosDTO> scrape(String ticker) {
        final String url = buildUrl(ticker);
        return executarComPlaywright(ticker, url);
    }
    
    /**
     * Método de fallback quando o Circuit Breaker está aberto ou há falhas.
     * Utiliza Selenium como alternativa ao Playwright para FIIs.
     */
    public Mono<FiiDadosFinanceirosDTO> fallbackToSelenium(String ticker, Exception ex) {
        logger.warn("Fallback FII para Selenium ativado. Ticker: {}, Causa: {}", 
                   ticker, ex.getClass().getSimpleName());
        return seleniumFallback.scrape(ticker);
    }

    private Mono<FiiDadosFinanceirosDTO> executarComPlaywright(String ticker, String url) {
        // refs para fechar com segurança em cancel/erro/sucesso
        AtomicReference<BrowserContext> ctxRef = new AtomicReference<>();
        AtomicReference<Page> pageRef = new AtomicReference<>();

        return createReactiveStructureForMono(() -> {
            logger.info("Iniciando scraping Playwright: {}", url);

            Browser browser = pwInit.getBrowser(); // singleton já inicializado

            // Usar métodos da classe base para configuração
            BrowserContext ctx = createPlaywrightContext(browser);
            Page page = createPlaywrightPage(ctx);

            ctxRef.set(ctx);
            pageRef.set(page);

            // Navegar e validar usando método da classe base
            navigateAndValidate(page, url, ticker);

            // Captura de XHR por substring (sem regex)
            final Map<String, String> urlsMapeadas = new HashMap<>();
            page.onRequest(req -> {
                String u = req.url();
                for (String chave : TODAS_AS_CHAVES) {
                    if (!urlsMapeadas.containsKey(chave) && u.contains(chave)) {
                        urlsMapeadas.put(chave, u);
                        logger.info("API capturada ({}): {}", chave, u);
                    }
                }
            });

            // Esperas curtas por cada endpoint (polling leve com timeout)
            waitForKey(urlsMapeadas, HISTORICO_INDICADORES, 12_000);
            waitForKey(urlsMapeadas, DIVIDENDOS,           12_000);
            waitForKey(urlsMapeadas, COTACAO,               12_000);

            // HTML para parsers existentes
            String html = page.content();
            Document doc = Jsoup.parse(html);
            
            // Validar elementos essenciais usando método da classe base
            validateEssentialElements(doc, ESSENTIAL_SELECTORS, CARDS_SELECTORS, ticker, url);

            // Parsers (compatíveis com Selenium/Playwright)
            FiiInfoHeaderDTO infoHeader = headerScraper.scrape(doc);
            FiiInfoSobreDTO infoSobre  = infoSobreScraper.scrape(doc);
            FiiInfoCardsDTO infoCards  = cardsScraper.scrape(doc);

            // ID interno via URLs capturadas
            Integer internalId = internalIdScrapper.scrape(new ArrayList<>(urlsMapeadas.values()));

            // Monos das APIs com fallback seguro
            Mono<FiiCotacaoDTO> cotacaoMono = Optional.ofNullable(urlsMapeadas.get(COTACAO))
                    .map(apiScraper::fetchCotacao)
                    .orElse(Mono.just(new FiiCotacaoDTO(null, null)));

            Mono<List<FiiDividendoDTO>> dividendosMono = Optional.ofNullable(urlsMapeadas.get(DIVIDENDOS))
                    .map(apiScraper::fetchDividendos)
                    .orElse(Mono.just(Collections.emptyList()));

            Mono<FiiIndicadorHistoricoDTO> historicoMono = Optional.ofNullable(urlsMapeadas.get(HISTORICO_INDICADORES))
                    .map(apiScraper::fetchHistorico)
                    .orElse(Mono.just(new FiiIndicadorHistoricoDTO(Collections.emptyMap())));

            // Composição final
            return Mono.zip(cotacaoMono, dividendosMono, historicoMono)
                    .map(t -> new FiiDadosFinanceirosDTO(
                            internalId,
                            infoHeader,
                            t.getT3(), // historico
                            infoSobre,
                            infoCards,
                            t.getT2(), // dividendos
                            t.getT1()  // cotacao
                    ));
        }, ticker, () -> closePlaywrightResources(pageRef.get(), ctxRef.get()));
    }

    /** Espera passiva (polling leve) até uma chave aparecer no mapa, com timeout em ms. */
    private static void waitForKey(Map<String, String> map, String key, int timeoutMs) {
        long deadline = System.nanoTime() + timeoutMs * 1_000_000L;
        while (System.nanoTime() < deadline) {
            if (map.containsKey(key)) return;
            try { Thread.sleep(40); } catch (InterruptedException ignored) {}
        }
        // Timeout é aceitável — os Monos já têm fallback default
    }

    // Template methods implementation
    
    @Override
    protected String[] getEssentialSelectors() {
        return ESSENTIAL_SELECTORS;
    }
    
    @Override
    protected String[] getCardsSelectors() {
        return CARDS_SELECTORS;
    }
    
    @Override
    protected String buildUrl(String ticker) {
        return "https://investidor10.com.br/fiis/" + ticker;
    }
    
    @Override
    protected FiiDadosFinanceirosDTO executeSpecificScraping(Document doc, String ticker) {
        // Este método não é usado diretamente no FII pois há lógica específica de APIs
        // Mantido para compatibilidade com a classe abstrata
        throw new UnsupportedOperationException("FII scraping usa lógica específica com APIs");
    }
}
