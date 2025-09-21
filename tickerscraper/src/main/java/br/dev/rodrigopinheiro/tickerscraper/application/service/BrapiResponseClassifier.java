package br.dev.rodrigopinheiro.tickerscraper.application.service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivoFinanceiroVariavel;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BrapiResponseClassifier {
     // Padrões para ETFs Nacionais
    private static final List<String> ETF_NACIONAL_PATTERNS = Arrays.asList(
        "index fund", "indice", "index", "bovespa", "ibovespa", "ishares brasil"
    );
    
    // Padrões para ETF BDRs (ETFs Estrangeiros terminados em 11)
    private static final List<String> ETF_BDR_PATTERNS = Arrays.asList(
        "investimento no exterior", "fundo de investimento de indice",
        "etf", "s&p 500", "nasdaq", "ishares core", "vanguard", "spdr"
    );
    
    // Padrões para FIIs
    private static final List<String> FII_PATTERNS = Arrays.asList(
        "fundo de investimento imobiliario", "fii", "real estate", "imobiliario",
        "shopping", "logistica", "corporativo", "residencial"
    );
    
    // Padrões para Units
    private static final List<String> UNIT_PATTERNS = Arrays.asList(
        "unit", "certificado de deposito", "units"
    );

    /**
     * Classifica ativo baseado na resposta da API Brapi
     */
    public TipoAtivoFinanceiroVariavel classificarPorResposta(BrapiQuoteResponse response) {
        if (response == null || response.results() == null || response.results().isEmpty()) {
            log.warn("Resposta Brapi vazia ou inválida");
            return TipoAtivoFinanceiroVariavel.DESCONHECIDO;
        }
        
        var quote = response.results().get(0);
        String textoCompleto = normalizar(
            (quote.shortName() != null ? quote.shortName() : "") + " " +
            (quote.longName() != null ? quote.longName() : "")
        );
        
        log.debug("Classificando ticker {} com texto: '{}'", quote.symbol(), textoCompleto);
        
        // Ordem específica: ETF BDR primeiro (mais específico)
        if (isETFBdr(textoCompleto)) {
            log.info("Ticker {} classificado como ETF_BDR", quote.symbol());
            return TipoAtivoFinanceiroVariavel.ETF_BDR;
        }
        
        if (isETFNacional(textoCompleto)) {
            log.info("Ticker {} classificado como ETF", quote.symbol());
            return TipoAtivoFinanceiroVariavel.ETF;
        }
        
        if (isFII(textoCompleto)) {
            log.info("Ticker {} classificado como FII", quote.symbol());    
            return TipoAtivoFinanceiroVariavel.FII;
        }
        
        if (isUnit(textoCompleto)) {
            log.info("Ticker {} classificado como UNIT", quote.symbol());
            return TipoAtivoFinanceiroVariavel.UNIT;
        }
        
        // Default para ação ordinária
        log.info("Ticker {} classificado como ACAO_ON (default)", quote.symbol());
        return TipoAtivoFinanceiroVariavel.ACAO_ON;
    }
    
    private boolean isETFBdr(String texto) {
        return ETF_BDR_PATTERNS.stream()
            .anyMatch(pattern -> texto.contains(pattern));
    }
    
    private boolean isETFNacional(String texto) {
        return ETF_NACIONAL_PATTERNS.stream()
            .anyMatch(pattern -> texto.contains(pattern));
    }
    
    private boolean isFII(String texto) {
        return FII_PATTERNS.stream()
            .anyMatch(pattern -> texto.contains(pattern));
    }
    
    private boolean isUnit(String texto) {
        return UNIT_PATTERNS.stream()
            .anyMatch(pattern -> texto.contains(pattern));
    }
    
    /**
     * Normaliza texto removendo acentos e convertendo para minúsculas
     */
    private String normalizar(String texto) {
        if (texto == null) return "";
        
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            .toLowerCase()
            .trim();
    }

}
