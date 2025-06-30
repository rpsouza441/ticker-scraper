package br.dev.rodrigopinheiro.tickerscraper.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    //Aumenta limite para 16MB
    private static final int MAX_BUFFER_SIZE = 16 * 1024 * 1024;

    @Bean
    public WebClient webClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(MAX_BUFFER_SIZE)
                )
                .build();
        return WebClient.builder()
                .exchangeStrategies(strategies)
                .build();
    }
}
