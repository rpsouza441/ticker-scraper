package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.selenium.retry;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v138.network.Network;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Manager responsável por executar operações Selenium com mecanismos de retry robustos.
 * 
 * Esta classe encapsula a lógica de retry para as três operações mais críticas
 * dos scrapers Selenium:
 * 1. Inicialização do WebDriver
 * 2. Carregamento de páginas
 * 3. Configuração de captura de rede (DevTools)
 * 
 * Utiliza Resilience4j para implementar políticas de retry configuráveis
 * com backoff exponencial e logging detalhado.
 */
@Component
public class SeleniumRetryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SeleniumRetryManager.class);
    
    private final SeleniumRetryConfiguration retryConfig;
    private final Retry webDriverRetry;
    private final Retry pageLoadRetry;
    private final Retry networkCaptureRetry;
    
    public SeleniumRetryManager(SeleniumRetryConfiguration retryConfig) {
        this.retryConfig = retryConfig;
        
        // Validar configurações na inicialização
        if (!retryConfig.isValid()) {
            throw new IllegalArgumentException("Configurações de retry inválidas: " + retryConfig);
        }
        
        this.webDriverRetry = createWebDriverRetry();
        this.pageLoadRetry = createPageLoadRetry();
        this.networkCaptureRetry = createNetworkCaptureRetry();
        
        logger.info("SeleniumRetryManager inicializado com configurações: {}", retryConfig);
    }
    
    /**
     * Cria uma instância do ChromeDriver com retry automático.
     * 
     * Tenta inicializar o WebDriver até o número máximo de tentativas configurado,
     * aplicando backoff entre as tentativas para lidar com problemas temporários
     * como falta de recursos ou conflitos de porta.
     * 
     * @param options Opções do Chrome configuradas
     * @return ChromeDriver inicializado
     * @throws WebDriverException se todas as tentativas falharem
     */
    public ChromeDriver createWebDriverWithRetry(ChromeOptions options) {
        return webDriverRetry.executeSupplier(() -> {
            logger.debug("Tentando inicializar ChromeDriver com opções: {}", options.asMap().keySet());
            
            ChromeDriver driver = new ChromeDriver(options);
            
            // Validar se o driver foi criado corretamente
            String sessionId = driver.getSessionId().toString();
            logger.debug("ChromeDriver inicializado com sucesso. SessionId: {}", sessionId);
            
            return driver;
        });
    }
    
    /**
     * Carrega uma página com retry automático e validação de carregamento completo.
     * 
     * Executa a navegação e aguarda o carregamento completo da página,
     * reaplicando a operação em caso de timeouts ou falhas de rede.
     * 
     * @param driver ChromeDriver ativo
     * @param url URL da página a ser carregada
     * @throws TimeoutException se todas as tentativas falharem
     */
    public void loadPageWithRetry(ChromeDriver driver, String url) {
        pageLoadRetry.executeRunnable(() -> {
            logger.debug("Carregando página: {}", url);
            
            // Navegar para a URL
            driver.get(url);
            
            // Aguardar carregamento completo usando JavaScript
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(webDriver -> {
                String readyState = ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState")
                    .toString();
                
                boolean isComplete = "complete".equals(readyState);
                
                if (!isComplete) {
                    logger.debug("Página ainda carregando. ReadyState: {}", readyState);
                }
                
                return isComplete;
            });
            
            // Aguardar um pouco mais para garantir que scripts assíncronos terminem
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrompido durante aguardo de carregamento", e);
            }
            
            logger.debug("Página carregada com sucesso: {}", url);
        });
    }
    
    /**
     * Configura DevTools para captura de rede com retry automático.
     * 
     * Inicializa uma sessão DevTools e habilita a captura de eventos de rede,
     * reaplicando a configuração em caso de falhas de conexão ou timeout.
     * 
     * @param driver ChromeDriver ativo
     * @return DevTools configurado
     * @throws WebDriverException se todas as tentativas falharem
     */
    public DevTools setupDevToolsWithRetry(ChromeDriver driver) {
        return networkCaptureRetry.executeSupplier(() -> {
            logger.debug("Configurando DevTools para captura de rede...");
            
            DevTools devTools = driver.getDevTools();
            
            // Criar sessão DevTools
            devTools.createSession();
            logger.debug("Sessão DevTools criada com sucesso");
            
            // Habilitar captura de rede
            devTools.send(Network.enable(
                Optional.empty(), // maxTotalBufferSize
                Optional.empty(), // maxResourceBufferSize  
                Optional.empty(), // maxRequestBufferSize
                Optional.empty() // maxPostDataSize
            ));
            
            logger.debug("Captura de rede habilitada no DevTools");
            
            return devTools;
        });
    }
    
    /**
     * Cria a política de retry para inicialização do WebDriver.
     * 
     * Configurada para lidar com exceções específicas do WebDriver como
     * SessionNotCreatedException e WebDriverException.
     */
    private Retry createWebDriverRetry() {
        Retry retry = Retry.of("selenium-webdriver", 
            RetryConfig.custom()
                .maxAttempts(retryConfig.getWebDriverMaxAttempts())
                .waitDuration(retryConfig.getWebDriverBackoff())
                .retryExceptions(
                    WebDriverException.class,
                    SessionNotCreatedException.class,
                    TimeoutException.class,
                    RuntimeException.class
                )
                .ignoreExceptions(
                    IllegalArgumentException.class,
                    SecurityException.class
                )
                .build());
        
        // Configurar listeners após criação do Retry
        if (retryConfig.isEnableRetryLogging()) {
            retry.getEventPublisher().onRetry(event -> {
                logger.warn("Retry WebDriver - Tentativa {} de {}: {}", 
                    event.getNumberOfRetryAttempts(), 
                    retryConfig.getWebDriverMaxAttempts(),
                    event.getLastThrowable().getMessage());
            });
            
            retry.getEventPublisher().onSuccess(event -> {
                if (event.getNumberOfRetryAttempts() > 0) {
                    logger.info("WebDriver inicializado com sucesso após {} tentativas", 
                        event.getNumberOfRetryAttempts());
                }
            });
        }
        
        return retry;
    }
    
    /**
     * Cria a política de retry para carregamento de páginas.
     * 
     * Configurada para lidar com timeouts e problemas de rede durante
     * a navegação e carregamento de páginas.
     */
    private Retry createPageLoadRetry() {
        Retry retry = Retry.of("selenium-pageload", 
            RetryConfig.custom()
                .maxAttempts(retryConfig.getPageLoadMaxAttempts())
                .waitDuration(retryConfig.getPageLoadBackoff())
                .retryExceptions(
                    TimeoutException.class,
                    WebDriverException.class,
                    NoSuchElementException.class
                )
                .ignoreExceptions(
                    IllegalArgumentException.class,
                    SecurityException.class
                )
                .build());
        
        // Configurar listeners após criação do Retry
        if (retryConfig.isEnableRetryLogging()) {
            retry.getEventPublisher().onRetry(event -> {
                logger.warn("Retry PageLoad - Tentativa {} de {}: {}", 
                    event.getNumberOfRetryAttempts(), 
                    retryConfig.getPageLoadMaxAttempts(),
                    event.getLastThrowable().getMessage());
            });
            
            retry.getEventPublisher().onSuccess(event -> {
                if (event.getNumberOfRetryAttempts() > 0) {
                    logger.info("Página carregada com sucesso após {} tentativas", 
                        event.getNumberOfRetryAttempts());
                }
            });
        }
        
        return retry;
    }
    
    /**
     * Cria a política de retry para configuração de captura de rede.
     * 
     * Configurada para lidar com falhas na inicialização do DevTools
     * e problemas de conexão com o protocolo Chrome DevTools.
     */
    private Retry createNetworkCaptureRetry() {
        Retry retry = Retry.of("selenium-network", 
            RetryConfig.custom()
                .maxAttempts(retryConfig.getNetworkCaptureMaxAttempts())
                .waitDuration(retryConfig.getNetworkCaptureBackoff())
                .retryExceptions(
                    WebDriverException.class,
                    TimeoutException.class,
                    RuntimeException.class
                )
                .ignoreExceptions(
                    IllegalArgumentException.class,
                    SecurityException.class,
                    UnsupportedOperationException.class
                )
                .build());
        
        // Configurar listeners após criação do Retry
        if (retryConfig.isEnableRetryLogging()) {
            retry.getEventPublisher().onRetry(event -> {
                logger.warn("Retry NetworkCapture - Tentativa {} de {}: {}", 
                    event.getNumberOfRetryAttempts(), 
                    retryConfig.getNetworkCaptureMaxAttempts(),
                    event.getLastThrowable().getMessage());
            });
            
            retry.getEventPublisher().onSuccess(event -> {
                if (event.getNumberOfRetryAttempts() > 0) {
                    logger.info("DevTools configurado com sucesso após {} tentativas", 
                        event.getNumberOfRetryAttempts());
                }
            });
        }
        
        return retry;
    }
    
    /**
     * Retorna as configurações de retry atualmente em uso.
     * 
     * @return Configurações de retry
     */
    public SeleniumRetryConfiguration getRetryConfiguration() {
        return retryConfig;
    }
}