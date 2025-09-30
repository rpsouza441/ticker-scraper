package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr;

import java.util.List;

// Torne a classe pública para ser acessível de qualquer lugar
public final class BdrApiConstants {
    private BdrApiConstants() {}

    // Torne as constantes públicas e estáticas
    public static final String HIST_INDICADORES = "/api/bdr/historico-indicadores/";
    public static final String DRE = "/api/international/balancos/balancoresultados/";
    public static final String BALANCO_PATRIMONIAL = "/api/international/balancos/balanco-patrimonial/";
    public static final String FLUXO_CAIXA = "/api/international/fluxo-caixa/";
    public static final String DIVIDENDOS = "/api/bdr/dividendos/";

    public static final List<String> TODAS_AS_CHAVES = List.of(
            HIST_INDICADORES,
            DRE,
            BALANCO_PATRIMONIAL,
            FLUXO_CAIXA,
            DIVIDENDOS
    );
}