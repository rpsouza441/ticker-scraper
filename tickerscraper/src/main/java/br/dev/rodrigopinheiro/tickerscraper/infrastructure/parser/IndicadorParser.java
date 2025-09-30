package br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser;

import org.mapstruct.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.RoundingMode;

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
    
    // Constantes para multiplicadores de escala
    private static final BigDecimal MILHAO = new BigDecimal("1000000");
    private static final BigDecimal BILHAO = new BigDecimal("1000000000");
    private static final BigDecimal TRILHAO = new BigDecimal("1000000000000");
    
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
     * Converte uma String (ex: "R$ 4,78", "79,97%", "1.234.567.000", "R$ 10,00 T", "9,78 B") para um BigDecimal.
     * Este método deve ser usado para TODOS os valores numéricos.
     * Suporta sufixos de escala: M (milhões), B (bilhões), T (trilhões).
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
                        return processarComSufixoEscala(numericPart);
                    }
                    return processarComSufixoEscala(s);
                })
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
     * Processa uma string que pode conter sufixos de escala (M, B, T) e retorna o BigDecimal correspondente.
     * 
     * @param texto A string a ser processada.
     * @return O valor como BigDecimal com a escala aplicada.
     */
    private static BigDecimal processarComSufixoEscala(String texto) {
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO;
        }
        
        // Primeiro, identifica o multiplicador baseado no sufixo
        String textoOriginalUpper = texto.toUpperCase().trim();
        BigDecimal multiplicador = BigDecimal.ONE;
        String textoParaLimpeza = texto;
        
        if (textoOriginalUpper.endsWith(" T") || textoOriginalUpper.endsWith("T")) {
            multiplicador = TRILHAO;
            // Remove o sufixo antes de limpar
            textoParaLimpeza = texto.replaceAll("(?i)\\s*T\\s*$", "").trim();
        } else if (textoOriginalUpper.endsWith(" B") || textoOriginalUpper.endsWith("B")) {
            multiplicador = BILHAO;
            // Remove o sufixo antes de limpar
            textoParaLimpeza = texto.replaceAll("(?i)\\s*B\\s*$", "").trim();
        } else if (textoOriginalUpper.endsWith(" M") || textoOriginalUpper.endsWith("M")) {
            multiplicador = MILHAO;
            // Remove o sufixo antes de limpar
            textoParaLimpeza = texto.replaceAll("(?i)\\s*M\\s*$", "").trim();
        }
        
        // Agora limpa o texto sem o sufixo
        String textoLimpo = limparTextoNumerico(textoParaLimpeza);
        
        // Valida se o texto limpo é um número válido
        if (!PADRAO_NUMERO.matcher(textoLimpo).matches()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal valor = converterParaBigDecimal(textoLimpo);
        return valor.multiply(multiplicador);
    }

    /**
     * Converte uma string limpa para BigDecimal.
     * 
     * @param valor A string já limpa e validada.
     * @return O valor como BigDecimal.
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
     * Representação simplificada da paridade de um BDR.
     */
    public record ParidadeBdrInfo(BigDecimal quantidadeBdr,
                                  BigDecimal quantidadeAcoes,
                                  BigDecimal fatorConversao,
                                  String moedaReferencia,
                                  String descricaoOriginal) {}

    /**
     * Faz o parsing de um valor monetário retornando um BigDecimal se possível.
     */
    public static Optional<BigDecimal> parseValorMonetario(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String texto = raw.replace("US$", "")
                .replace("R$", "")
                .replace("$", "")
                .replaceAll("(?i)[A-Z]{3}", "")
                .trim();

        if (texto.isEmpty() || !texto.matches(".*\\d.*")) {
            return Optional.empty();
        }

        BigDecimal valor = parseBigdecimal(texto);
        return Optional.ofNullable(valor);
    }

    /**
     * Tenta extrair uma moeda a partir de um texto.
     */
    public static Optional<String> extrairMoeda(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        if (raw.contains("R$")) {
            return Optional.of("BRL");
        }
        if (raw.contains("US$")) {
            return Optional.of("USD");
        }
        Matcher matcher = Pattern.compile("(?i)\\b([A-Z]{3})\\b").matcher(raw.replace("$", ""));
        if (matcher.find()) {
            return Optional.of(matcher.group(1).toUpperCase());
        }
        return Optional.empty();
    }

    /**
     * Converte percentuais representados como string para decimal (ex: "12,5%" -> 0.125).
     */
    public static Optional<BigDecimal> parsePercentualParaDecimal(String raw) {
        if (raw == null || !raw.contains("%")) {
            return Optional.empty();
        }
        String texto = raw.replace("%", "")
                .replace("a.a", "")
                .replace("ao ano", "")
                .trim();
        if (texto.isEmpty() || !texto.matches(".*\\d.*")) {
            return Optional.empty();
        }
        BigDecimal valor = parseBigdecimal(texto);
        return Optional.of(valor.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));
    }

    /**
     * Faz o parsing de paridade textual, retornando a razão entre BDR e ação original.
     */
    public static Optional<ParidadeBdrInfo> parseParidadeBdr(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        Pattern pattern = Pattern.compile("(?i)([0-9.,]+)\\s*BDR.*?=\\s*([0-9.,]+)");
        Matcher matcher = pattern.matcher(raw);
        if (!matcher.find()) {
            return Optional.empty();
        }
        String bdrTexto = matcher.group(1);
        String acaoTexto = matcher.group(2);
        if (!bdrTexto.matches(".*\\d.*") || !acaoTexto.matches(".*\\d.*")) {
            return Optional.empty();
        }
        BigDecimal quantidadeBdr = parseBigdecimal(bdrTexto);
        BigDecimal quantidadeAcao = parseBigdecimal(acaoTexto);
        if (quantidadeBdr == null || quantidadeAcao == null ||
                quantidadeBdr.compareTo(BigDecimal.ZERO) <= 0 ||
                quantidadeAcao.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }
        BigDecimal fator = quantidadeAcao.divide(quantidadeBdr, 8, RoundingMode.HALF_UP);
        String moeda = extrairMoeda(raw).orElse(null);
        return Optional.of(new ParidadeBdrInfo(quantidadeBdr, quantidadeAcao, fator, moeda, raw.trim()));
    }

    /**
     * Faz parsing seguro de números representados como texto.
     */
    public static Optional<Double> safeParseDouble(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String texto = limparTextoNumerico(raw);
        if (texto.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Double.parseDouble(texto));
        } catch (NumberFormatException e) {
            logger.debug("Não foi possível converter '{}' para double: {}", raw, e.getMessage());
            return Optional.empty();
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
    @Named("normalizar")
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