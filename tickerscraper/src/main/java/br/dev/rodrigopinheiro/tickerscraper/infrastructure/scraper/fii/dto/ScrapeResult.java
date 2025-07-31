package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;


import java.util.Map;
import java.util.Optional;

public  record ScrapeResult(String html, Map<String, String> urlsMapeadas) {
    public Optional<String> findUrl(String keyword) {
        return Optional.ofNullable(urlsMapeadas.get(keyword));
    }
}