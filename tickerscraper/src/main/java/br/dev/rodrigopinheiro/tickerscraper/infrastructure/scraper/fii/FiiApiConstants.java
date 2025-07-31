package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

public class FiiApiConstants {

    // Privado para impedir que a classe seja instanciada.
    private FiiApiConstants() {}

    public static final String DIVIDENDOS = "dividendos/chart";
    public static final String HISTORICO_INDICADORES = "historico-indicadores";
    public static final String COTACAO = "cotacao/fii";

    // Você também pode colocar a lista pré-montada aqui para reutilização.
    public static final java.util.List<String> TODAS_AS_CHAVES = java.util.List.of(
            DIVIDENDOS,
            HISTORICO_INDICADORES,
            COTACAO
    );
}
