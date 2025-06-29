package br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class IndicadorParser {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final NumberFormat FORMATADOR = NumberFormat.getNumberInstance(LOCALE_BR);

    public static BigDecimal parseValorMonetario(String raw) {
        if (raw == null || raw.isBlank()) return BigDecimal.ZERO;
        try {
            String limpo = raw.replace("R$", "").replace("%", "").trim();
            Number numero = FORMATADOR.parse(limpo);
            return new BigDecimal(numero.toString());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Erro ao converter valor: " + raw, e);
        }
    }

    public static double parseDoublePercentual(String raw) {
        return parseValorMonetario(raw).doubleValue();
    }

    public static BigDecimal parseOrNull(String raw) {
        try {
            return parseValorMonetario(raw);
        } catch (Exception e) {
            return null;
        }
    }
}