package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.selenium.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuração específica para mecanismos de retry nos scrapers Selenium.
 * 
 * Esta classe centraliza todas as configurações de retry para operações críticas
 * do Selenium como inicialização do WebDriver, carregamento de páginas e captura de rede.
 * 
 * As configurações são externalizadas via application.yml permitindo ajustes
 * sem necessidade de recompilação.
 */
@Configuration
@ConfigurationProperties(prefix = "scraper.selenium.retry")
public class SeleniumRetryConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(SeleniumRetryConfiguration.class);
    
    // Configurações para WebDriver initialization
    private int webDriverMaxAttempts = 3;
    private Duration webDriverBackoff = Duration.ofSeconds(2);
    
    // Configurações para page loading
    private int pageLoadMaxAttempts = 5;
    private Duration pageLoadBackoff = Duration.ofMillis(500);
    
    // Configurações para network capture
    private int networkCaptureMaxAttempts = 3;
    private Duration networkCaptureBackoff = Duration.ofSeconds(1);
    
    // Configurações gerais
    private boolean enableRetryLogging = true;
    private Duration maxRetryDuration = Duration.ofMinutes(2);
    
    public SeleniumRetryConfiguration() {
        logger.info("Inicializando configurações de retry para Selenium");
    }
    
    // Getters e Setters
    
    public int getWebDriverMaxAttempts() {
        return webDriverMaxAttempts;
    }
    
    public void setWebDriverMaxAttempts(int webDriverMaxAttempts) {
        this.webDriverMaxAttempts = webDriverMaxAttempts;
    }
    
    public Duration getWebDriverBackoff() {
        return webDriverBackoff;
    }
    
    public void setWebDriverBackoff(Duration webDriverBackoff) {
        this.webDriverBackoff = webDriverBackoff;
    }
    
    public int getPageLoadMaxAttempts() {
        return pageLoadMaxAttempts;
    }
    
    public void setPageLoadMaxAttempts(int pageLoadMaxAttempts) {
        this.pageLoadMaxAttempts = pageLoadMaxAttempts;
    }
    
    public Duration getPageLoadBackoff() {
        return pageLoadBackoff;
    }
    
    public void setPageLoadBackoff(Duration pageLoadBackoff) {
        this.pageLoadBackoff = pageLoadBackoff;
    }
    
    public int getNetworkCaptureMaxAttempts() {
        return networkCaptureMaxAttempts;
    }
    
    public void setNetworkCaptureMaxAttempts(int networkCaptureMaxAttempts) {
        this.networkCaptureMaxAttempts = networkCaptureMaxAttempts;
    }
    
    public Duration getNetworkCaptureBackoff() {
        return networkCaptureBackoff;
    }
    
    public void setNetworkCaptureBackoff(Duration networkCaptureBackoff) {
        this.networkCaptureBackoff = networkCaptureBackoff;
    }
    
    public boolean isEnableRetryLogging() {
        return enableRetryLogging;
    }
    
    public void setEnableRetryLogging(boolean enableRetryLogging) {
        this.enableRetryLogging = enableRetryLogging;
    }
    
    public Duration getMaxRetryDuration() {
        return maxRetryDuration;
    }
    
    public void setMaxRetryDuration(Duration maxRetryDuration) {
        this.maxRetryDuration = maxRetryDuration;
    }
    
    /**
     * Valida se as configurações estão dentro de limites aceitáveis.
     * 
     * @return true se as configurações são válidas
     */
    public boolean isValid() {
        return webDriverMaxAttempts > 0 && webDriverMaxAttempts <= 10 &&
               pageLoadMaxAttempts > 0 && pageLoadMaxAttempts <= 20 &&
               networkCaptureMaxAttempts > 0 && networkCaptureMaxAttempts <= 10 &&
               webDriverBackoff != null && !webDriverBackoff.isNegative() &&
               pageLoadBackoff != null && !pageLoadBackoff.isNegative() &&
               networkCaptureBackoff != null && !networkCaptureBackoff.isNegative();
    }
    
    @Override
    public String toString() {
        return "SeleniumRetryConfiguration{" +
                "webDriverMaxAttempts=" + webDriverMaxAttempts +
                ", webDriverBackoff=" + webDriverBackoff +
                ", pageLoadMaxAttempts=" + pageLoadMaxAttempts +
                ", pageLoadBackoff=" + pageLoadBackoff +
                ", networkCaptureMaxAttempts=" + networkCaptureMaxAttempts +
                ", networkCaptureBackoff=" + networkCaptureBackoff +
                ", enableRetryLogging=" + enableRetryLogging +
                ", maxRetryDuration=" + maxRetryDuration +
                '}';
    }
}