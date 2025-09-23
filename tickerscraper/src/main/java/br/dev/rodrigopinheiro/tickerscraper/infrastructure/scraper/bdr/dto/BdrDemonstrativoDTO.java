package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Representa um demonstrativo financeiro (DRE, BP ou FC) capturado via API internacional.
 */
public record BdrDemonstrativoDTO(
        String tipo,
        JsonNode raw
) {
    public static BdrDemonstrativoDTO empty(String tipo) {
        return new BdrDemonstrativoDTO(tipo, null);
    }
}
