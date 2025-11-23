package br.dev.rodrigopinheiro.tickerscraper.infrastructure.mapper;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class BdrScraperMapperTest {

    @Test
    void testIndicadorParserWithMSFT34Values() {
        // Testa diretamente o IndicadorParser com os valores do MSFT34
        IndicadorParser parser = new IndicadorParser();
        
        // Valores reais do MSFT34
        String pEbitValue = "30.16";
        String pEbitdaValue = "23.73";
        String pAtivoValue = "6.14";
        
        // Testa o parsing
        BigDecimal pEbitParsed = parser.parseBigdecimal(pEbitValue);
        BigDecimal pEbitdaParsed = parser.parseBigdecimal(pEbitdaValue);
        BigDecimal pAtivoParsed = parser.parseBigdecimal(pAtivoValue);
        
        // Verifica se os valores foram mapeados corretamente
        System.out.println("=== Valores parseados do MSFT34 ===");
        System.out.println("P/EBIT: " + pEbitValue + " -> " + pEbitParsed);
        System.out.println("P/EBITDA: " + pEbitdaValue + " -> " + pEbitdaParsed);
        System.out.println("P/ATIVO: " + pAtivoValue + " -> " + pAtivoParsed);
        
        // Verifica se os valores estão corretos (não multiplicados por 100)
        assertEquals(new BigDecimal("30.16"), pEbitParsed);
        assertEquals(new BigDecimal("23.73"), pEbitdaParsed);
        assertEquals(new BigDecimal("6.14"), pAtivoParsed);
        
        System.out.println("✅ Todos os valores estão corretos!");
    }
}