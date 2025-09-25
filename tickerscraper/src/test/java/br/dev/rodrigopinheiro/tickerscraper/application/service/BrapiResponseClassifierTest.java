package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResponse;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrapiResponseClassifierTest {

    private BrapiResponseClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new BrapiResponseClassifier();
    }

    @ParameterizedTest
    @CsvSource({
        "'iShares Core S&P 500', 'ETF que investe no exterior', ETF_BDR",
        "'Fundo de Investimento Imobiliário', 'FII Shopping', FII",
        "'iShares Bovespa', 'Index Fund Brasil', ETF",
        "'Petrobras Unit', 'Certificado de Depósito', UNIT",
        "'Petrobras', 'Empresa de petróleo', ACAO_ON"
    })
    void deveClassificarCorretamente(String shortName, String longName, TipoAtivo esperado) {
        var response = criarBrapiResponse("TEST11", shortName, longName);
        
        var resultado = classifier.classificarPorResposta(response);
        
        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void deveRetornarDesconhecidoParaRespostaVazia() {
        var resultado = classifier.classificarPorResposta(null);
        assertThat(resultado).isEqualTo(TipoAtivo.DESCONHECIDO);
    }

    private BrapiQuoteResponse criarBrapiResponse(String symbol, String shortName, String longName) {
        var result = new BrapiQuoteResult(
            symbol,
            shortName,
            longName,
            "BRL",
            100.0,
            "1000000000",
            "https://example.com/logo.png"
        );
        
        var response = new BrapiQuoteResponse(
            List.of(result),
            "2024-01-01T10:00:00Z",
            "100ms"
        );
        
        return response;
    }
}