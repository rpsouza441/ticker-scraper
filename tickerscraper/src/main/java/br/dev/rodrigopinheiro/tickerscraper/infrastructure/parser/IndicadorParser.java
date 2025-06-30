package br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser;


public class IndicadorParser {

    /**
     * Limpa uma string de indicadores, removendo R$, %, e espaços nas extremidades.
     * O resultado é retornado como uma String limpa.
     *
     * @param raw A string bruta extraída do HTML (ex: "  R$ 15,30  ", "25,00 % ").
     * @return Uma string limpa (ex: "15,30", "25,00") ou uma string vazia se a entrada for nula/vazia.
     */
    public static String limparTextoIndicador(String raw) {
        if (raw == null || raw.isBlank()) {
            return ""; // Retorna uma string vazia como padrão para dados ausentes
        }
        // Remove os símbolos e espaços. A ordem não importa.
        return raw.replace("R$", "")
                .replace("%", "")
                .trim(); // .trim() remove espaços no início e no fim
    }

    /**
     * Versão mais avançada que também padroniza separadores numéricos.
     * Transforma "1.234,56" em "1234.56".
     */
    public static String limparTextoNumerico(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return raw.replace("R$", "")
                .replace(".", "")      // Remove o separador de milhar
                .replace(",", ".")      // Substitui a vírgula decimal por ponto
                .replace("%", "")
                .trim();
    }
}