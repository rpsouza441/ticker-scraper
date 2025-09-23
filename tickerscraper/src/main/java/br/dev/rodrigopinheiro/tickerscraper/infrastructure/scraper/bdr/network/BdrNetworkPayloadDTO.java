package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.network;

import java.util.Map;

/**
 * Resultado da captura de rede contendo o investidorId e os payloads crus das APIs.
 */
public record BdrNetworkPayloadDTO(String investidorId, Map<String, String> payloads) {
}
