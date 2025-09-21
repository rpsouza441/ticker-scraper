package br.dev.rodrigopinheiro.tickerscraper.adapter.output.http;

import br.dev.rodrigopinheiro.tickerscraper.domain.exception.NetworkCaptureException;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.BrapiHttpClient;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResponse;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrapiClassificadorAdapter - Testes de Classificação via API")
class BrapiClassificadorAdapterTest {

    @Mock
    private BrapiHttpClient brapiClient;

    private BrapiClassificadorAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BrapiClassificadorAdapter(brapiClient);
    }

    @ParameterizedTest
    @MethodSource("provideTipoClassificacaoData")
    @DisplayName("Deve classificar corretamente baseado na resposta da API Brapi")
    void deveClassificarCorretamenteBaseadoNaResposta(String ticker, String shortName, String longName, TipoAtivo expectedTipo) {
        // Arrange
        BrapiQuoteResult result = new BrapiQuoteResult(ticker, shortName, longName, "BRL", 10.0, "1B", "logo.png");
        BrapiQuoteResponse response = new BrapiQuoteResponse(List.of(result), "2025-01-01", "100ms");
        
        when(brapiClient.getQuote(ticker)).thenReturn(Mono.just(response));

        // Act & Assert
        StepVerifier.create(adapter.classificarPorApi(ticker))
            .expectNext(expectedTipo)
            .verifyComplete();
    }

    @Test
    @DisplayName("Deve retornar vazio quando API não retorna resultados")
    void deveRetornarVazioQuandoApiNaoRetornaResultados() {
        // Arrange
        BrapiQuoteResponse response = new BrapiQuoteResponse(List.of(), "2025-01-01", "100ms");
        when(brapiClient.getQuote("INVALID")).thenReturn(Mono.just(response));

        // Act & Assert
        StepVerifier.create(adapter.classificarPorApi("INVALID"))
            .verifyComplete();
    }

    @Test
    @DisplayName("Deve retornar vazio quando API falha")
    void deveRetornarVazioQuandoApiFalha() {
        // Arrange
        when(brapiClient.getQuote(anyString()))
            .thenReturn(Mono.error(new NetworkCaptureException("PETR4", "API indisponível", new RuntimeException())));

        // Act & Assert
        StepVerifier.create(adapter.classificarPorApi("PETR4"))
            .verifyComplete();
    }

    @Test
    @DisplayName("Deve retornar vazio para ticker nulo ou vazio")
    void deveRetornarVazioParaTickerNuloOuVazio() {
        // Act & Assert
        StepVerifier.create(adapter.classificarPorApi(null))
            .verifyComplete();

        StepVerifier.create(adapter.classificarPorApi(""))
            .verifyComplete();

        StepVerifier.create(adapter.classificarPorApi("   "))
            .verifyComplete();
    }

    @Test
    @DisplayName("Deve verificar disponibilidade da API corretamente")
    void deveVerificarDisponibilidadeDaApiCorretamente() {
        // Arrange - API disponível
        BrapiQuoteResult result = new BrapiQuoteResult("PETR4", "PETROBRAS", "PETROBRAS S.A.", "BRL", 30.0, "100B", "logo.png");
        BrapiQuoteResponse response = new BrapiQuoteResponse(List.of(result), "2025-01-01", "100ms");
        when(brapiClient.getQuote("PETR4")).thenReturn(Mono.just(response));

        // Act & Assert
        StepVerifier.create(adapter.isApiDisponivel())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    @DisplayName("Deve retornar false quando API está indisponível")
    void deveRetornarFalseQuandoApiEstaIndisponivel() {
        // Arrange
        when(brapiClient.getQuote("PETR4"))
            .thenReturn(Mono.error(new NetworkCaptureException("PETR4", "API indisponível", new RuntimeException())));

        // Act & Assert
        StepVerifier.create(adapter.isApiDisponivel())
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    @DisplayName("Deve classificar FII por padrão quando nome não é conclusivo")
    void deveClassificarFiiPorPadraoQuandoNomeNaoEConclusivo() {
        // Arrange
        BrapiQuoteResult result = new BrapiQuoteResult("HGLG11", "CSHG LOGÍSTICA", "CSHG LOGÍSTICA FII", "BRL", 100.0, "1B", "logo.png");
        BrapiQuoteResponse response = new BrapiQuoteResponse(List.of(result), "2025-01-01", "100ms");
        
        when(brapiClient.getQuote("HGLG11")).thenReturn(Mono.just(response));

        // Act & Assert
        StepVerifier.create(adapter.classificarPorApi("HGLG11"))
            .expectNext(TipoAtivo.FII)
            .verifyComplete();
    }

    private static Stream<Arguments> provideTipoClassificacaoData() {
        return Stream.of(
            // Classificação por nome - FII
            Arguments.of("HGLG11", "CSHG LOGÍSTICA FII", "CSHG LOGÍSTICA FUNDO DE INVESTIMENTO IMOBILIÁRIO", TipoAtivo.FII),
            Arguments.of("XPML11", "XP MALLS FII", "XP MALLS FUNDO DE INVESTIMENTO IMOBILIÁRIO", TipoAtivo.FII),
            Arguments.of("BCFF11", "FUNDO IMOBILIÁRIO", "BRASIL CAPITAL FUNDO DE INVESTIMENTO IMOBILIÁRIO", TipoAtivo.FII),
            
            // Classificação por nome - ETF
            Arguments.of("BOVA11", "BOVA11 ETF", "ISHARES IBOVESPA FUNDO DE ÍNDICE", TipoAtivo.ETF),
            Arguments.of("SMAL11", "SMAL11 ETF", "ISHARES SMALL CAP FUNDO DE ÍNDICE", TipoAtivo.ETF),
            
            // Classificação por padrão - FII (fallback)
            Arguments.of("ABCD11", "ABCD LOGÍSTICA", "ABCD LOGÍSTICA S.A.", TipoAtivo.FII),
            Arguments.of("EFGH11", "EFGH PARTICIPAÇÕES", "EFGH PARTICIPAÇÕES LTDA", TipoAtivo.FII),
            

            
            // Casos ambíguos - DESCONHECIDO
            Arguments.of("ABCD1", "ABCD", "ABCD PARTICIPAÇÕES", TipoAtivo.DESCONHECIDO),
            Arguments.of("EFGH2", "EFGH", "EFGH HOLDINGS", TipoAtivo.DESCONHECIDO),
            Arguments.of("XYZW9", "XYZW", "XYZW INVESTIMENTOS", TipoAtivo.DESCONHECIDO)
        );
    }
}