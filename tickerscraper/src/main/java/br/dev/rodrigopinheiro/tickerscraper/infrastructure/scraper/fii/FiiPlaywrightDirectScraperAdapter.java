package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.PlaywrightInitializer;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base.AbstractScraperAdapter;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.common.CorrelationIdProvider;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiCotacaoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDividendoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiIndicadorHistoricoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoCardsDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoHeaderDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoSobreDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.CapturedRequest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
    private final CorrelationIdProvider correlationIdProvider; // Injeção de dependência para correlationId

    public FiiPlaywrightDirectScraperAdapter(
            PlaywrightInitializer pwInit,
            FiiSeleniumScraperAdapter seleniumFallback,
            FiiHeaderScraper headerScraper,
            FiiInfoSobreScraper infoSobreScraper,
            FiiCardsScraper cardsScraper,
            FiiInternalIdScrapper internalIdScrapper,
            FiiApiScraper apiScraper,
            CorrelationIdProvider correlationIdProvider
    ) {
        this.pwInit = pwInit;
        this.seleniumFallback = seleniumFallback;
        this.headerScraper = headerScraper;
        this.infoSobreScraper = infoSobreScraper;
        this.cardsScraper = cardsScraper;
        this.internalIdScrapper = internalIdScrapper;
        this.apiScraper = apiScraper;
        this.correlationIdProvider = correlationIdProvider;
        
        logger.info("FiiPlaywrightDirectScraperAdapter inicializado com injeção de dependências SOLID-compliant");
    }

    @Override
    @CircuitBreaker(name = "scraper", fallbackMethod = "fallbackToSelenium")
    @Retry(name = "scraper")
    public Mono<FiiDadosFinanceirosDTO> scrape(String ticker) {
        logger.info("Iniciando scraping FII com APIs assíncronas para: {}", ticker);
        
        // Tenta scraping completo com APIs primeiro
        return scrapeWithAsyncApis(ticker)
                .onErrorResume(ex -> {
                    logger.warn("Falha no scraping com APIs para {}: {}. Tentando scraping básico.", 
                               ticker, ex.getMessage());
                    
                    // Fallback para scraping básico usando a classe base
                    final String url = buildUrl(ticker);
                    return createReactiveStructure(() -> {
                        // Usa implementação da classe base para scraping simples
                        Browser browser = pwInit.getBrowser();
                        BrowserContext ctx = createPlaywrightContext(browser);
                        Page page = createPlaywrightPage(ctx);
                        
                        try {
                            navigateAndValidate(page, url, ticker);
                            String html = page.content();
                            Document doc = Jsoup.parse(html);
                            validateEssentialElements(doc, ESSENTIAL_SELECTORS, CARDS_SELECTORS, ticker, url);
                            
                            return executeSpecificScraping(doc, ticker);
                        } finally {
                            closePlaywrightResources(page, ctx);
                        }
                    }, ticker, () -> {});
                });
    }
    
    /**
     * Métod de fallback quando o Circuit Breaker está aberto ou há falhas.
     * Utiliza Selenium como alternativa ao Playwright para FIIs.
     */
    public Mono<FiiDadosFinanceirosDTO> fallbackToSelenium(String ticker, Exception ex) {
        logger.warn("Fallback FII para Selenium ativado. Ticker: {}, Causa: {}", 
                   ticker, ex.getClass().getSimpleName());
        return seleniumFallback.scrape(ticker);
    }
    
    /**
     * Scraping completo com APIs assíncronas.
     * Separação de responsabilidades (SRP) - métod específico para operações assíncronas.
     */
    private Mono<FiiDadosFinanceirosDTO> scrapeWithAsyncApis(String ticker) {
        final String url = buildUrl(ticker);
        return executarComPlaywright(ticker, url);
    }

    private Mono<FiiDadosFinanceirosDTO> executarComPlaywright(String ticker, String url) {
        // refs para fechar com segurança em cancel/erro/sucesso
        AtomicReference<BrowserContext> ctxRef = new AtomicReference<>();
        AtomicReference<Page> pageRef = new AtomicReference<>();
        
        // Usa constantes padronizadas da classe base (DIP)
        final Duration asyncTimeout = Duration.ofMillis(ASYNC_OPERATION_TIMEOUT_MS);
        final Duration apiTimeout = Duration.ofMillis(API_CALL_TIMEOUT_MS);
        final Duration networkTimeout = Duration.ofMillis(NETWORK_CAPTURE_TIMEOUT_MS);
        
        // Usa injeção de dependência para correlationId (DIP)
        final String correlationId = correlationIdProvider.getCurrentCorrelationIdOrDefault("unknown");

        return createReactiveStructureForMono(() -> {
            logger.info("Iniciando scraping Playwright: {} ", url);

            try {
                Browser browser = pwInit.getBrowser(); // singleton já inicializado

                // Usar métodos da classe base para configuração
                BrowserContext ctx = createPlaywrightContext(browser);
                Page page = createPlaywrightPage(ctx);

                ctxRef.set(ctx);
                pageRef.set(page);

                // Captura de XHR por substring (sem regex)
                final Map<String, CapturedRequest> requestsMapeadas = new ConcurrentHashMap<>();
                page.onRequest(req -> {
                    String u = req.url();
                    Map<String, String> headers = req.headers();
                    for (String chave : TODAS_AS_CHAVES) {
                        if (!requestsMapeadas.containsKey(chave) && u.contains(chave)) {
                            requestsMapeadas.put(chave, new CapturedRequest(u, headers));
                            logger.info("API capturada ({}): {} ", chave, u);
                        }
                    }
                });

                // Navegar e validar usando método da classe base
                navigateAndValidate(page, url, ticker);

                // Captura paralela de APIs para reduzir tempo de 30s para ~10s (60% de redução)
                logger.info("Iniciando captura paralela de APIs para {} ", ticker);
                
                    CompletableFuture<Void> historicoFuture = CompletableFuture.runAsync(() ->
                    waitForKeyWithTimeout(requestsMapeadas, HISTORICO_INDICADORES, NETWORK_CAPTURE_TIMEOUT_MS, ticker, correlationId));

                CompletableFuture<Void> dividendosFuture = CompletableFuture.runAsync(() ->
                    waitForKeyWithTimeout(requestsMapeadas, DIVIDENDOS, NETWORK_CAPTURE_TIMEOUT_MS, ticker, correlationId));

                CompletableFuture<Void> cotacaoFuture = CompletableFuture.runAsync(() ->
                    waitForKeyWithTimeout(requestsMapeadas, COTACAO, NETWORK_CAPTURE_TIMEOUT_MS, ticker, correlationId));
                
                try {
                    // Espera todas as APIs simultaneamente com timeout máximo
                    CompletableFuture.allOf(historicoFuture, dividendosFuture, cotacaoFuture)
                        .get(NETWORK_CAPTURE_TIMEOUT_MS + 2000, java.util.concurrent.TimeUnit.MILLISECONDS);
                    
                    logger.info("Captura paralela concluída para {} ", ticker);
                } catch (java.util.concurrent.TimeoutException e) {
                    logger.warn("Timeout na captura paralela para {} após {}ms ", 
                               ticker, NETWORK_CAPTURE_TIMEOUT_MS + 2000);
                } catch (Exception e) {
                    logger.warn("Erro na captura paralela para {}: {} ", 
                               ticker, e.getMessage());
                }

                // HTML para parsers existentes
                String html = page.content();
                Document doc = Jsoup.parse(html);
                
                // Validar elementos essenciais usando method da classe base
                validateEssentialElements(doc, ESSENTIAL_SELECTORS, CARDS_SELECTORS, ticker, url);

                // Parsers (compatíveis com Selenium/Playwright)
                FiiInfoHeaderDTO infoHeader = headerScraper.scrape(doc);
                FiiInfoSobreDTO infoSobre  = infoSobreScraper.scrape(doc);
                FiiInfoCardsDTO infoCards  = cardsScraper.scrape(doc);

                // ID interno via URLs capturadas
                Integer internalId = internalIdScrapper.scrape(
                        requestsMapeadas.values().stream().map(CapturedRequest::url).toList());

                // Monos das APIs com fallback seguro e timeout padronizado
                Mono<FiiCotacaoDTO> cotacaoMono = Optional.ofNullable(requestsMapeadas.get(COTACAO))
                        .map(req -> apiScraper.fetchCotacao(req.url(), req.headers())
                                .timeout(apiTimeout)
                                .doOnError(ex -> logger.warn("Timeout na API de cotação para {}: {}", ticker, ex.getMessage()))
                                .onErrorReturn(new FiiCotacaoDTO(null, null)))
                        .orElse(Mono.just(new FiiCotacaoDTO(null, null)));

                Mono<List<FiiDividendoDTO>> dividendosMono = Optional.ofNullable(requestsMapeadas.get(DIVIDENDOS))
                        .map(req -> apiScraper.fetchDividendos(req.url(), req.headers())
                                .timeout(apiTimeout)
                                .doOnError(ex -> logger.warn("Timeout na API de dividendos para {}: {}", ticker, ex.getMessage()))
                                .onErrorReturn(Collections.emptyList()))
                        .orElse(Mono.just(Collections.emptyList()));

                Mono<FiiIndicadorHistoricoDTO> historicoMono = Optional.ofNullable(requestsMapeadas.get(HISTORICO_INDICADORES))
                        .map(req -> apiScraper.fetchHistorico(req.url(), req.headers())
                                .timeout(apiTimeout)
                                .doOnError(ex -> logger.warn("Timeout na API de histórico para {}: {}", ticker, ex.getMessage()))
                                .onErrorReturn(new FiiIndicadorHistoricoDTO(Collections.emptyMap())))
                        .orElse(Mono.just(new FiiIndicadorHistoricoDTO(Collections.emptyMap())));

                // Composição final com timeout global padronizado
                return Mono.zip(cotacaoMono, dividendosMono, historicoMono)
                        .timeout(asyncTimeout)
                        .map(t -> new FiiDadosFinanceirosDTO(
                                internalId,
                                infoHeader,
                                t.getT3(), // historico
                                infoSobre,
                                infoCards,
                                t.getT2(), // dividendos
                                t.getT1()  // cotacao
                        ))
                        .doOnError(java.util.concurrent.TimeoutException.class, 
                                ex -> logger.error("Timeout geral no scraping Playwright para {}: {}s ", 
                                          ticker, asyncTimeout.getSeconds()))
                        .onErrorMap(java.util.concurrent.TimeoutException.class, 
                                ex -> AsyncRequestTimeoutException.forPlaywrightScraping(ticker, asyncTimeout, correlationId));
                        
            } catch (Exception ex) {
                logger.error("Erro durante inicialização do Playwright para {}: {} ", 
                           ticker, ex.getMessage());
                throw ex;
            }
        }, ticker, () -> closePlaywrightResources(pageRef.get(), ctxRef.get()));
    }

    /** Espera passiva (polling leve) até uma chave aparecer no mapa, com timeout em ms. */
    private static void waitForKey(Map<String, ?> map, String key, int timeoutMs) {
        long deadline = System.nanoTime() + timeoutMs * 1_000_000L;
        while (System.nanoTime() < deadline) {
            if (map.containsKey(key)) return;
            try { Thread.sleep(40); } catch (InterruptedException ignored) {}
        }
        // Timeout é aceitável — os Monos já têm fallback default
    }
    
    /**
     * Versão melhorada do waitForKey com logging detalhado e tratamento de timeout.
     */
    private static void waitForKeyWithTimeout(Map<String, ?> map, String key, int timeoutMs,
                                            String ticker, String correlationId) {
        int waited = 0;
        final int checkInterval = 200;
        
        logger.debug("Aguardando captura da API '{}' para ticker {} (timeout: {}ms) ", 
                    key, ticker, timeoutMs);
        
        while (!map.containsKey(key) && waited < timeoutMs) {
            try { 
                Thread.sleep(checkInterval); 
            } catch (InterruptedException e) { 
                Thread.currentThread().interrupt(); 
                logger.warn("Interrompido durante espera da API '{}' para ticker {} ", 
                           key, ticker);
                break; 
            }
            waited += checkInterval;
            
            // Log de progresso a cada 2 segundos
            if (waited % 2000 == 0) {
                logger.debug("Ainda aguardando API '{}' para ticker {}: {}ms/{}ms ", 
                           key, ticker, waited, timeoutMs);
            }
        }
        
        if (map.containsKey(key)) {
            logger.debug("API '{}' capturada para ticker {} em {}ms ", 
                        key, ticker, waited);
        } else {
            logger.warn("Timeout na captura da API '{}' para ticker {} após {}ms ", 
                       key, ticker, waited);
        }
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
        logger.debug("Executando scraping básico (somente HTML) para FII: {}", ticker);
        
        // Scraping síncrono básico usando apenas dados HTML
        // Implementa corretamente o contrato da classe base (SRP + LSP)
        FiiInfoHeaderDTO infoHeader = headerScraper.scrape(doc);
        FiiInfoSobreDTO infoSobre = infoSobreScraper.scrape(doc);
        FiiInfoCardsDTO infoCards = cardsScraper.scrape(doc);
        
        // Retorna DTO básico sem dados de APIs (fallback seguro)
        return new FiiDadosFinanceirosDTO(
            null, // internalId - não disponível sem URLs de API
            infoHeader,
            new FiiIndicadorHistoricoDTO(Collections.emptyMap()), // histórico vazio
            infoSobre,
            infoCards,
            Collections.emptyList(), // dividendos vazios
            new FiiCotacaoDTO(null, null) // cotação vazia
        );
    }
}
