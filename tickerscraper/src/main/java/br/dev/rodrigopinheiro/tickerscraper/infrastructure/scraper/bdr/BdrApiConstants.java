package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr;

import java.util.Set;

/**
 * Constantes utilitárias para captura das requisições XHR executadas pela página de BDRs.
 */
public final class BdrApiConstants {
    public static final String BASE_URL = "https://investidor10.com.br/bdrs/";

    public static final String COTACOES_CHART_PATH = "/api/bdr/cotacoes/chart/";
    public static final String DIVIDENDOS_PATH = "/api/bdr/dividendos/chart/";
    public static final String HISTORICO_INDICADORES_PATH = "/api/bdr/historico-indicadores/";
    public static final String BALANCO_DRE_PATH = "/api/international/balancos/";

    public static final String KEY_COTACOES = "cotacoes";
    public static final String KEY_DIVIDENDOS = "dividendos";
    public static final String KEY_INDICADORES = "indicadores";
    public static final String KEY_DRE = "dre";
    public static final String KEY_BP = "bp";
    public static final String KEY_FC = "fc";

    public static final Set<String> ALL_KEYS = Set.of(
            KEY_COTACOES,
            KEY_DIVIDENDOS,
            KEY_INDICADORES,
            KEY_DRE,
            KEY_BP,
            KEY_FC
    );

    private BdrApiConstants() {
    }
}
