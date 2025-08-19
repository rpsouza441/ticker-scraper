package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base;

import br.dev.rodrigopinheiro.tickerscraper.domain.exception.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Classe abstrata base para adapters de scraping que consolida funcionalidades comuns
 * entre implementações Playwright e Selenium.
 * 
 * Fornece métodos padronizados para:
 * - Configuração de browsers (Playwright e Selenium)
 * - Validação de elementos HTML essenciais
 * - Tratamento de erros HTTP e detecção de tickers não encontrados
 * - Limpeza segura de recursos
 * - Estrutura reativa consistente
 * 
 * @param <T> Tipo do DTO de dados financeiros retornado pelo scraper
 */
public abstract class AbstractScraperAdapter<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractScraperAdapter.class);
    
    // Configurações padrão para browsers
    protected static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36";
    protected static final int DEFAULT_VIEWPORT_WIDTH = 1920;
    protected static final int DEFAULT_VIEWPORT_HEIGHT = 1080;
    protected static final int DEFAULT_TIMEOUT_MS = 15_000;
    protected static final String DEFAULT_LOCALE = "pt-BR";
    protected static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";
    
    // Configurações padronizadas de timeout para operações assíncronas (OTIMIZADAS)
    protected static final int ASYNC_OPERATION_TIMEOUT_MS = 20_000;  // Reduzido de 45s para 20s (56% redução)
    protected static final int API_CALL_TIMEOUT_MS = 8_000;          // Reduzido de 15s para 8s (47% redução)
    protected static final int NETWORK_CAPTURE_TIMEOUT_MS = 5_000;   // Reduzido de 10s para 5s (50% redução)
    protected static final int ELEMENT_WAIT_TIMEOUT_MS = 8_000;      // Reduzido de 10s para 8s (20% redução)
    
    /**
     * Cria e configura um contexto Playwright com configurações anti-bot básicas.
     * 
     * @param browser Instância do browser Playwright
     * @return Contexto configurado
     */
    protected BrowserContext createPlaywrightContext(Browser browser) {
        Browser.NewContextOptions ctxOpts = new Browser.NewContextOptions()
                .setUserAgent(DEFAULT_USER_AGENT)
                .setViewportSize(DEFAULT_VIEWPORT_WIDTH, DEFAULT_VIEWPORT_HEIGHT)
                .setLocale(DEFAULT_LOCALE)
                .setTimezoneId(DEFAULT_TIMEZONE)
                .setExtraHTTPHeaders(Map.of("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7"));
        
        BrowserContext ctx = browser.newContext(ctxOpts);
        ctx.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        
        return ctx;
    }
    
    /**
     * Cria e configura uma página Playwright com timeout padrão.
     * 
     * @param context Contexto do browser
     * @return Página configurada
     */
    protected Page createPlaywrightPage(BrowserContext context) {
        Page page = context.newPage();
        page.setDefaultTimeout(DEFAULT_TIMEOUT_MS);
        return page;
    }
    
    /**
     * Cria e configura ChromeOptions com configurações padrão para scraping.
     * 
     * @return ChromeOptions configurado
     */
    protected ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=" + DEFAULT_VIEWPORT_WIDTH + "," + DEFAULT_VIEWPORT_HEIGHT);
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("user-agent=" + DEFAULT_USER_AGENT);
        
        return options;
    }
    
    /**
     * Navega para uma URL usando Playwright e valida a resposta HTTP.
     * 
     * @param page Página Playwright
     * @param url URL de destino
     * @param ticker Ticker sendo processado
     * @return Resposta HTTP
     * @throws TickerNotFoundException se ticker não for encontrado (404/410)
     * @throws ScrapingException para outros erros HTTP
     * @throws ScrapingTimeoutException para timeouts
     */
    protected Response navigateAndValidate(Page page, String url, String ticker) {
        try {
            Response response = page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            
            if (response != null) {
                int status = response.status();
                if (status == 404 || status == 410) {
                    logger.warn("Ticker {} não encontrado - HTTP {}: {}", ticker, status, response.statusText());
                    throw new TickerNotFoundException(ticker, url);
                } else if (status >= 400) {
                    logger.warn("Erro HTTP {} para ticker {}: {}", status, ticker, response.statusText());
                    throw createHttpException(status, response.statusText(), ticker, url);
                }
            }
            
            return response;
        } catch (TimeoutError e) {
            throw new ScrapingTimeoutException(ticker, url, Duration.ofSeconds(15), "NAVIGATION");
        }
    }
    
    /**
     * Valida se elementos essenciais estão presentes no documento HTML.
     * 
     * @param doc Documento HTML
     * @param essentialSelectors Seletores de elementos essenciais
     * @param cardsSelectors Seletores de elementos de cards
     * @param ticker Ticker sendo processado
     * @param url URL da página
     * @throws TickerNotFoundException se nenhum elemento essencial for encontrado
     */
    protected void validateEssentialElements(Document doc, String[] essentialSelectors, 
                                           String[] cardsSelectors, String ticker, String url) {
        boolean hasEssentialElements = validateElementsExist(doc, essentialSelectors);
        boolean hasCardsElements = validateElementsExist(doc, cardsSelectors);
        
        if (!hasEssentialElements && !hasCardsElements) {
            logger.error("Nenhum elemento essencial encontrado para ticker {} - possível ticker inexistente", ticker);
            
            // Verificar se a página contém indicadores de erro
            String html = doc.html();
            if (html.contains("410 Gone") || html.contains("Not Found") || 
                html.contains("Página não encontrada") || html.contains("Ticker não encontrado")) {
                throw new TickerNotFoundException(ticker, url);
            }
            
            // Se não há elementos essenciais, assumir ticker inexistente
            throw new TickerNotFoundException(ticker, url);
        }
        
        if (!hasEssentialElements) {
            logger.warn("Nenhum elemento essencial encontrado para ticker {} com seletores: {}", 
                       ticker, java.util.Arrays.toString(essentialSelectors));
        }
        
        if (!hasCardsElements) {
            logger.warn("Nenhum elemento de cards encontrado para ticker {} com seletores: {}", 
                       ticker, java.util.Arrays.toString(cardsSelectors));
        }
    }
    
    /**
     * Verifica se pelo menos um dos seletores encontra elementos no documento.
     * 
     * @param doc Documento HTML
     * @param selectors Array de seletores CSS
     * @return true se pelo menos um elemento for encontrado
     */
    protected boolean validateElementsExist(Document doc, String[] selectors) {
        if (selectors == null || selectors.length == 0) {
            return false;
        }
        
        for (String selector : selectors) {
            if (!doc.select(selector).isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Espera por qualquer um dos seletores fornecidos em uma página Playwright.
     * 
     * @param page Página Playwright
     * @param selectors Array de seletores CSS
     * @param timeoutMs Timeout em milissegundos
     * @param ticker Ticker sendo processado
     * @param url URL da página
     * @return true se algum elemento for encontrado
     */
    protected boolean waitForAnySelector(Page page, String[] selectors, int timeoutMs, String ticker, String url) {
        for (String selector : selectors) {
            try {
                page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
                logger.debug("Elemento encontrado com seletor: {} para ticker: {}", selector, ticker);
                return true;
            } catch (TimeoutError e) {
                // Continua tentando os próximos seletores
                continue;
            } catch (Exception e) {
                // Outros erros podem indicar anti-bot
                if (e.getMessage().contains("blocked") || e.getMessage().contains("captcha")) {
                    throw new AntiBotDetectedException(ticker, url, e.getMessage(), "Playwright");
                }
                // Para outros erros, continua tentando
                continue;
            }
        }
        return false; // Nenhum seletor foi encontrado
    }
    
    /**
     * Fecha recursos Playwright de forma segura sem propagar exceções.
     * 
     * @param page Página a ser fechada
     * @param context Contexto a ser fechado
     */
    protected void closePlaywrightResources(Page page, BrowserContext context) {
        try { 
            if (page != null) page.close(); 
        } catch (Exception ignored) {}
        
        try { 
            if (context != null) context.close(); 
        } catch (Exception ignored) {}
    }
    
    /**
     * Fecha recursos Selenium de forma segura sem propagar exceções.
     * 
     * @param driver WebDriver a ser fechado
     * @param devTools DevTools a ser fechado (pode ser null)
     */
    protected void closeSeleniumResources(WebDriver driver, DevTools devTools) {
        if (devTools != null) {
            try {
                devTools.close();
            } catch (Exception e) {
                logger.warn("Erro ao fechar DevTools: {}", e.getMessage());
            }
        }
        
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                logger.warn("Erro ao fechar WebDriver: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Cria a estrutura reativa padrão para execução de scraping.
     * 
     * @param scrapingLogic Lógica de scraping a ser executada
     * @param ticker Ticker sendo processado
     * @param cleanupAction Ação de limpeza a ser executada
     * @return Mono com resultado do scraping
     */
    protected Mono<T> createReactiveStructure(java.util.concurrent.Callable<T> scrapingLogic, 
                                             String ticker, Runnable cleanupAction) {
        return Mono.fromCallable(scrapingLogic)
                .doOnError(e -> logger.error("Falha no scraping para {}: {}", ticker, e.toString()))
                .doOnCancel(cleanupAction)
                .doFinally(sig -> cleanupAction.run())
                .subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Cria a estrutura reativa para execução de scraping que retorna Mono.
     * 
     * @param scrapingLogic Lógica de scraping que retorna Mono
     * @param ticker Ticker sendo processado
     * @param cleanupAction Ação de limpeza a ser executada
     * @return Mono com resultado do scraping
     */
    protected Mono<T> createReactiveStructureForMono(java.util.concurrent.Callable<Mono<T>> scrapingLogic, 
                                                    String ticker, Runnable cleanupAction) {
        return Mono.fromCallable(scrapingLogic)
                .flatMap(mono -> mono)
                .doOnError(e -> logger.error("Falha no scraping para {}: {}", ticker, e.toString()))
                .doOnCancel(cleanupAction)
                .doFinally(sig -> cleanupAction.run())
                .subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Cria uma exceção HTTP específica.
     * 
     * @param status Status HTTP
     * @param statusText Texto do status HTTP
     * @param ticker Ticker sendo processado
     * @param url URL da requisição
     * @return Exceção HTTP configurada
     */
    private HttpScrapingException createHttpException(int status, String statusText, String ticker, String url) {
        return new HttpScrapingException(status, statusText, ticker, url);
    }
    
    // Template methods que devem ser implementados pelas subclasses
    
    /**
     * Retorna os seletores CSS para elementos essenciais específicos do tipo de scraper.
     * 
     * @return Array de seletores CSS
     */
    protected abstract String[] getEssentialSelectors();
    
    /**
     * Retorna os seletores CSS para elementos de cards específicos do tipo de scraper.
     * 
     * @return Array de seletores CSS
     */
    protected abstract String[] getCardsSelectors();
    
    /**
     * Constrói a URL base para o tipo de ticker (ação ou FII).
     * 
     * @param ticker Código do ticker
     * @return URL completa
     */
    protected abstract String buildUrl(String ticker);
    
    /**
     * Executa a lógica específica de scraping após validações básicas.
     * 
     * @param doc Documento HTML parseado
     * @param ticker Ticker sendo processado
     * @return DTO com dados extraídos
     */
    protected abstract T executeSpecificScraping(Document doc, String ticker);
}