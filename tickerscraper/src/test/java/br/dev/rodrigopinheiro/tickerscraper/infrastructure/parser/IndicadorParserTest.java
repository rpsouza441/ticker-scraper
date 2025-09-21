package br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class IndicadorParserTest {

    @Test
    void testParseBigDecimalComSufixoMilhoes() {
        // Testa valores com sufixo M (milhões)
        assertEquals(0, new BigDecimal("10000000").compareTo(IndicadorParser.parseBigdecimal("10 M")));
        assertEquals(0, new BigDecimal("10000000").compareTo(IndicadorParser.parseBigdecimal("10M")));
        assertEquals(0, new BigDecimal("1500000").compareTo(IndicadorParser.parseBigdecimal("1,5 M")));
        assertEquals(0, new BigDecimal("1500000").compareTo(IndicadorParser.parseBigdecimal("R$ 1,5 M")));
    }

    @Test
    void testParseBigDecimalComSufixoBilhoes() {
        // Testa valores com sufixo B (bilhões)
        assertEquals(0, new BigDecimal("10000000000").compareTo(IndicadorParser.parseBigdecimal("10 B")));
        assertEquals(0, new BigDecimal("10000000000").compareTo(IndicadorParser.parseBigdecimal("10B")));
        assertEquals(0, new BigDecimal("9780000000").compareTo(IndicadorParser.parseBigdecimal("9,78 B")));
        assertEquals(0, new BigDecimal("9780000000").compareTo(IndicadorParser.parseBigdecimal("R$ 9,78 B")));
    }

    @Test
    void testParseBigDecimalComSufixoTrilhoes() {
        // Testa valores com sufixo T (trilhões)
        assertEquals(0, new BigDecimal("10000000000000").compareTo(IndicadorParser.parseBigdecimal("10 T")));
        assertEquals(0, new BigDecimal("10000000000000").compareTo(IndicadorParser.parseBigdecimal("10T")));
        assertEquals(0, new BigDecimal("10000000000000").compareTo(IndicadorParser.parseBigdecimal("R$ 10,00 T")));
    }

    @Test
    void testParseBigDecimalSemSufixo() {
        // Testa que valores sem sufixo continuam funcionando
        assertEquals(0, new BigDecimal("55.84").compareTo(IndicadorParser.parseBigdecimal("55,84")));
        assertEquals(0, new BigDecimal("55.84").compareTo(IndicadorParser.parseBigdecimal("R$ 55,84")));
        assertEquals(0, new BigDecimal("3.87").compareTo(IndicadorParser.parseBigdecimal("3,87%")));
        assertEquals(0, new BigDecimal("9.20").compareTo(IndicadorParser.parseBigdecimal("9,20%")));
    }

    @Test
    void testParseBigDecimalValoresInvalidos() {
        // Testa que valores inválidos retornam zero
        assertEquals(0, BigDecimal.ZERO.compareTo(IndicadorParser.parseBigdecimal("")));
        assertEquals(0, BigDecimal.ZERO.compareTo(IndicadorParser.parseBigdecimal(null)));
        assertEquals(0, BigDecimal.ZERO.compareTo(IndicadorParser.parseBigdecimal("-")));
        assertEquals(0, BigDecimal.ZERO.compareTo(IndicadorParser.parseBigdecimal("abc")));
    }
}