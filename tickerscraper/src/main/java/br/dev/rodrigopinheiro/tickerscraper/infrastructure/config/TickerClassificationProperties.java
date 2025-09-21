package br.dev.rodrigopinheiro.tickerscraper.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ticker.classification")
public class TickerClassificationProperties {
    
    private Cache cache = new Cache();
    private Brapi brapi = new Brapi();
    private Database database = new Database();
    
    @Data
    public static class Cache {
        private boolean enabled = true;
        private int maxSize = 1000;
        private int ttlHours = 1;
    }
    
    @Data
    public static class Brapi {
        private boolean fallbackEnabled = true;
        private int timeoutSeconds = 10;
    }
    
    @Data
    public static class Database {
        private boolean parallelQueries = true;
    }
}