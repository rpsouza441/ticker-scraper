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
    void testParseBigDecimalComSufixosPortugues() {
        // Testa valores com sufixos em português (como aparecem nos dados do BDR)
        assertEquals(0, new BigDecimal("165220000000").compareTo(IndicadorParser.parseBigdecimal("165,22 Bilhões")));
        assertEquals(0, new BigDecimal("86600000000").compareTo(IndicadorParser.parseBigdecimal("86,60 Bilhões")));
        assertEquals(0, new BigDecimal("140740000000").compareTo(IndicadorParser.parseBigdecimal("140,74 Bilhões")));
        assertEquals(0, new BigDecimal("8470000000").compareTo(IndicadorParser.parseBigdecimal("8,47 Bilhões")));
        
        // Testa milhões em português
        assertEquals(0, new BigDecimal("500000000").compareTo(IndicadorParser.parseBigdecimal("500 Milhões")));
        assertEquals(0, new BigDecimal("1500000").compareTo(IndicadorParser.parseBigdecimal("1,5 Milhões")));
        
        // Testa trilhões em português
        assertEquals(0, new BigDecimal("2500000000000").compareTo(IndicadorParser.parseBigdecimal("2,5 Trilhões")));
        
        // Testa singular
        assertEquals(0, new BigDecimal("1000000000").compareTo(IndicadorParser.parseBigdecimal("1 Bilhão")));
        assertEquals(0, new BigDecimal("1000000").compareTo(IndicadorParser.parseBigdecimal("1 Milhão")));
        assertEquals(0, new BigDecimal("1000000000000").compareTo(IndicadorParser.parseBigdecimal("1 Trilhão")));
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