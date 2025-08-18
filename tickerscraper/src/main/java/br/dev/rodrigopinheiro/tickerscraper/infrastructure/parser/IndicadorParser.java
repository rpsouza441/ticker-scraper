package br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser;

import org.mapstruct.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Classe utilitária para processamento e normalização de textos extraídos de HTML,
 * especialmente focada em indicadores financeiros.
 */
public class IndicadorParser {
    private static final Logger logger = LoggerFactory.getLogger(IndicadorParser.class);
    
    // Constantes para padrões comuns de texto
    private static final String SIMBOLO_REAL = "R$";
    private static final String SIMBOLO_PERCENTUAL = "%";
    private static final Pattern PADRAO_NUMERO = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final String[] PADROES_TAXA_ANUAL = {" % a.a", " % ao ano", "% a.a", "% ao ano"};
    
    /**
     * Limpa uma string de indicadores, removendo R$, %, e espaços nas extremidades.
     * O resultado é retornado como uma String limpa.
     *
     * @param raw A string bruta extraída do HTML (ex: "  R$ 15,30  ", "25,00 % ").
     * @return Uma string limpa (ex: "15,30", "25,00") ou uma string vazia se a entrada for nula/vazia.
     */
    @Named("limparTextoIndicador")
    public static String limparTextoIndicador(String raw) {
        if (raw == null || raw.isBlank()) {
            return ""; // Retorna uma string vazia como padrão para dados ausentes
        }
        // Remove os símbolos e espaços. A ordem não importa.
        return raw.replace(SIMBOLO_REAL, "")
                .replace(SIMBOLO_PERCENTUAL, "")
                .trim(); // .trim() remove espaços no início e no fim
    }

    /**
     * Versão mais avançada que também padroniza separadores numéricos.
     * Transforma "1.234,56" em "1234.56".
     *
     * @param raw A string bruta extraída do HTML.
     * @return Uma string formatada para conversão numérica ou string vazia se a entrada for nula/vazia.
     */
    @Named("limpezaNumerica")
    public static String limparTextoNumerico(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return raw
                .replace(SIMBOLO_REAL, "")
                .replace(".", "")      // Remove o separador de milhar
                .replace(",", ".")      // Substitui a vírgula decimal por ponto
                .replace(SIMBOLO_PERCENTUAL, "")
                .trim();
    }

    /**
     * Converte texto para maiúsculas e remove espaços nas extremidades.
     *
     * @param str A string a ser processada.
     * @return A string em maiúsculas e sem espaços nas extremidades, ou null se a entrada for nula.
     */
    @Named("limpezaComUpperCase")
    public static String toUpperTrim(String str) {
        return str == null ? null : str.toUpperCase().trim();
    }

    /**
     * Converte uma String (ex: "R$ 4,78", "79,97%", "1.234.567.000") para um BigDecimal.
     * Este método deve ser usado para TODOS os valores numéricos.
     *
     * @param raw A string bruta extraída do HTML.
     * @return O valor como um BigDecimal. Retorna BigDecimal.ZERO se a entrada for nula, vazia ou inválida.
     */
    @Named("paraBigDecimal")
    public static BigDecimal parseBigdecimal(String raw) {
        return Optional.ofNullable(raw)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    // Tratamento especial para taxa de administração no formato "X,XX % a.a" ou "X,XX% a.a"
                    if (contemPadraoTaxaAnual(s)) {
                        // Extrai apenas a parte numérica antes do "%"
                        String numericPart = extrairParteNumericaAntesDe(s, SIMBOLO_PERCENTUAL);
                        return limparTextoNumerico(numericPart);
                    }
                    return limparTextoNumerico(s);
                })
                .filter(s -> PADRAO_NUMERO.matcher(s).matches())
                .map(s -> converterParaBigDecimal(s))
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Verifica se uma string contém algum dos padrões de taxa anual.
     *
     * @param texto O texto a ser verificado.
     * @return true se contiver algum padrão de taxa anual, false caso contrário.
     */
    private static boolean contemPadraoTaxaAnual(String texto) {
        for (String padrao : PADROES_TAXA_ANUAL) {
            if (texto.contains(padrao)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extrai a parte numérica antes de um delimitador específico.
     *
     * @param texto O texto completo.
     * @param delimitador O delimitador que marca o fim da parte numérica.
     * @return A parte numérica extraída.
     */
    private static String extrairParteNumericaAntesDe(String texto, String delimitador) {
        String espacoDelimitador = " " + delimitador;
        if (texto.contains(espacoDelimitador)) {
            return texto.split(espacoDelimitador)[0];
        } else {
            return texto.split(delimitador)[0];
        }
    }

    /**
     * Converte uma string para BigDecimal com tratamento de exceção.
     *
     * @param valor A string a ser convertida.
     * @return O BigDecimal correspondente ou BigDecimal.ZERO em caso de erro.
     */
    private static BigDecimal converterParaBigDecimal(String valor) {
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            logger.error("Erro ao converter '{}' para BigDecimal: {}", valor, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Normaliza uma string para um formato consistente: maiúsculas e sem acentos.
     * Ex: "Razão Social" -> "RAZAO SOCIAL"
     * Ex: "NÚMERO DE COTISTAS" -> "NUMERO DE COTISTAS"
     * 
     * @param texto O texto bruto a ser normalizado.
     * @return O texto normalizado.
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        // Normaliza para decompor os caracteres acentuados (ex: 'á' -> 'a' + '´')
        String textoSemAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD);
        // Remove os diacríticos (os acentos) usando uma expressão regular
        textoSemAcentos = textoSemAcentos.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // Converte para maiúsculas e remove espaços extras
        return textoSemAcentos.toUpperCase().trim();
    }
}