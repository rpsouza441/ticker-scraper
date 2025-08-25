package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;


import java.util.Map;
import java.util.Optional;

public  record ScrapeResult(String html, Map<String, CapturedRequest> requestsMapeadas) {
    public Optional<CapturedRequest> findRequest(String keyword) {
        return Optional.ofNullable(requestsMapeadas.get(keyword));
    }
}