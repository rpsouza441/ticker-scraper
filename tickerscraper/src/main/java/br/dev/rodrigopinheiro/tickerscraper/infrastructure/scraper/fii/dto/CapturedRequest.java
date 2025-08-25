package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto;

import java.util.Map;

/**
 * Representa uma requisição interceptada contendo a URL alvo e os
 * respectivos headers enviados pelo browser. Os headers incluem o
 * cabeçalho de cookies, permitindo que chamadas subsequentes para as
 * APIs reproduzam o mesmo contexto de requisição.
 */
public record CapturedRequest(String url, Map<String, String> headers) {
}

