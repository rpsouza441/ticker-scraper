package br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser;


import org.mapstruct.Named;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class IndicadorParser {
    // Define o formato numérico para o Brasil (entende "1.000,50")
    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final NumberFormat FORMATADOR = NumberFormat.getInstance(LOCALE_BR);

    /**
     * Limpa uma string de indicadores, removendo R$, %, e espaços nas extremidades.
     * O resultado é retornado como uma String limpa.
     *
     * @param raw A string bruta extraída do HTML (ex: "  R$ 15,30  ", "25,00 % ").
     * @return Uma string limpa (ex: "15,30", "25,00") ou uma string vazia se a entrada for nula/vazia.
     */
    @Named("limpezaSimples")
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
    @Named("limpezaNumerica")
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


    /**
     * Converte uma String (ex: "R$ 4,78", "79,97%", "1.234.567.000") para um BigDecimal.
     * Este method deve ser usado para TODOS os valores numéricos.
     *
     * @param raw A string bruta extraída do HTML.
     * @return O valor como um BigDecimal. Retorna BigDecimal.ZERO se a entrada for nula, vazia ou inválida.
     */
    @Named("paraBigDecimal")
    public static BigDecimal parseBigdecimal(String raw) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            String limpo = limparTextoNumerico(raw); // "1.234,56" → "1234.56"
            return new BigDecimal(limpo);
        } catch (NumberFormatException e) {
            System.err.println("Alerta: Erro ao converter valor para BigDecimal: '" + raw + "'. Retornando 0. Erro: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}