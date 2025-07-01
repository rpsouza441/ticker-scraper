package br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser;


import org.mapstruct.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

public class IndicadorParser {
    private static final Logger logger = LoggerFactory.getLogger(IndicadorParser.class);

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
        return raw
                .replace("R$", "")
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
        return Optional.ofNullable(raw)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(IndicadorParser::limparTextoNumerico)
                .filter(s -> s.matches("-?\\d+(\\.\\d+)?"))
                .map(s -> {
                    try {
                        return new BigDecimal(s);
                    } catch (NumberFormatException e) {
                        logger.error(e.getMessage());
                        return BigDecimal.ZERO;
                    }
                })
                .orElse(BigDecimal.ZERO);
    }
}