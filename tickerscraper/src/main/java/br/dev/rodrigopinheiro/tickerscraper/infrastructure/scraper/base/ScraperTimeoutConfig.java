package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configures timeout values for scraper operations using external properties.
 */
@Configuration
public class ScraperTimeoutConfig {

    public ScraperTimeoutConfig(
            @Value("${scraper.network-capture-timeout-ms:10000}") int networkCaptureTimeout) {
        AbstractScraperAdapter.NETWORK_CAPTURE_TIMEOUT_MS = networkCaptureTimeout;
    }
}
