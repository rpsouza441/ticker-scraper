package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;

import java.util.Map;

/**
 * Metadados básicos extraídos do HTML do BDR para auditoria.
 */
public record BdrHtmlMetadataDTO(
        String titulo,
        String descricao,
        Map<String, String> metaTags,
        String html
) {
    public static BdrHtmlMetadataDTO empty(String html) {
        return new BdrHtmlMetadataDTO(null, null, Map.of(), html);
    }
}
