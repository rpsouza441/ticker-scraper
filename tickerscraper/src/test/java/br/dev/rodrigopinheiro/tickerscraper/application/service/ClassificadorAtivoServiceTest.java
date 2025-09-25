package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.ClassificadorAtivoPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClassificadorAtivoService - Testes de Classificação Híbrida")
class ClassificadorAtivoServiceTest {

    @Mock
    private ClassificadorAtivoPort classificadorAtivoPort;
    
    private ClassificadorAtivoService classificadorService;

    @BeforeEach
    void setUp() {
        classificadorService = new ClassificadorAtivoService(classificadorAtivoPort);
    }

    @ParameterizedTest
    @CsvSource({
        "PETR3, ACAO_ON",
        "PETR4, ACAO_PN", 
        "VALE3, ACAO_ON",
        "VALE5, ACAO_PNA",
        "ITUB4, ACAO_PN",
        "BBDC4, ACAO_PN",
        "ABEV3, ACAO_ON",
        "MGLU3, ACAO_ON",
        "WEGE3, ACAO_ON",
        "RENT3, ACAO_ON"
    })
    @DisplayName("Deve classificar ações por heurística rapidamente")
    void shouldClassifyAcoesByHeuristicQuickly(String ticker, String expectedType) {
        // When
        Mono<TipoAtivo> result = classificadorService.classificar(ticker);
        
        // Then
        StepVerifier.create(result)
                .expectNext(TipoAtivo.valueOf(expectedType))
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "HGLG11", "XPML11", "KNRI11", "MXRF11", "BCFF11", "HGRE11",
        "BOVA11", "IVVB11", "SMAL11", "PIBB11", "ISUS11", "DIVO11",
        "PETR11", "VALE11", "ITUB11", "BBDC11", "ABEV11", "MGLU11"
    })
    @DisplayName("Deve marcar tickers ambíguos (final 11) como DESCONHECIDO temporariamente")
    void shouldMarkAmbiguousTickersAsDesconhecidoTemporarily(String ticker) {
        // Given - Configurar mock para retornar vazio (simulando API indisponível)
        when(classificadorAtivoPort.classificarPorApi(ticker)).thenReturn(Mono.empty());
        
        // When
        Mono<TipoAtivo> result = classificadorService.classificar(ticker);
        
        // Then
        StepVerifier.create(result)
                .expectNext(TipoAtivo.DESCONHECIDO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve retornar DESCONHECIDO para ticker null")
    void shouldReturnDesconhecidoForNullTicker() {
        // When
        Mono<TipoAtivo> result = classificadorService.classificar(null);
        
        // Then
        StepVerifier.create(result)
                .expectNext(TipoAtivo.DESCONHECIDO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve retornar DESCONHECIDO para ticker vazio")
    void shouldReturnDesconhecidoForEmptyTicker() {
        // When
        Mono<TipoAtivo> result = classificadorService.classificar("   ");
        
        // Then
        StepVerifier.create(result)
                .expectNext(TipoAtivo.DESCONHECIDO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve retornar DESCONHECIDO para ticker inválido")
    void shouldReturnDesconhecidoForInvalidTicker() {
        // When
        Mono<TipoAtivo> result = classificadorService.classificar("INVALID");
        
        // Then
        StepVerifier.create(result)
                .expectNext(TipoAtivo.DESCONHECIDO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve ser case insensitive")
    void shouldBeCaseInsensitive() {
        // When
        Mono<TipoAtivo> result1 = classificadorService.classificar("petr4");
        Mono<TipoAtivo> result2 = classificadorService.classificar("PETR4");
        Mono<TipoAtivo> result3 = classificadorService.classificar("Petr4");
        
        // Then
        StepVerifier.create(result1)
                .expectNext(TipoAtivo.ACAO_PN)
                .verifyComplete();
        
        StepVerifier.create(result2)
                .expectNext(TipoAtivo.ACAO_PN)
                .verifyComplete();
        
        StepVerifier.create(result3)
                .expectNext(TipoAtivo.ACAO_PN)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve trimar espaços em branco")
    void shouldTrimWhitespace() {
        // When
        Mono<TipoAtivo> result = classificadorService.classificar("  PETR4  ");
        
        // Then
        StepVerifier.create(result)
                .expectNext(TipoAtivo.ACAO_PN)
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "HGLG11", "XPML11", "KNRI11", "MXRF11", "BCFF11", "HGRE11",
        "BOVA11", "IVVB11", "SMAL11", "PIBB11", "ISUS11", "DIVO11",
        "PETR11", "VALE11", "ITUB11", "BBDC11", "ABEV11", "MGLU11"
    })
    @DisplayName("Deve identificar corretamente tickers que precisam consultar API")
    void shouldCorrectlyIdentifyTickersThatNeedApiConsultation(String ticker) {
        // When
        boolean needsApi = classificadorService.precisaConsultarApi(ticker);
        
        // Then
        assertThat(needsApi).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "PETR3", "PETR4", "VALE3", "VALE5", "ITUB4", "BBDC4", "ABEV3"
    })
    @DisplayName("Deve identificar corretamente tickers que NÃO precisam consultar API")
    void shouldCorrectlyIdentifyTickersThatDontNeedApiConsultation(String ticker) {
        // When
        boolean needsApi = classificadorService.precisaConsultarApi(ticker);
        
        // Then
        assertThat(needsApi).isFalse();
    }

    @Test
    @DisplayName("Deve classificar lote de tickers corretamente")
    void shouldClassifyBatchOfTickersCorrectly() {
        // Given - Configurar mocks para tickers ambíguos
        when(classificadorAtivoPort.classificarPorApi("HGLG11")).thenReturn(Mono.empty());
        when(classificadorAtivoPort.classificarPorApi("BOVA11")).thenReturn(Mono.empty());
        
        List<String> tickers = Arrays.asList("PETR4", "VALE3", "HGLG11", "ITUB4", "BOVA11");
        
        // When
        Flux<TipoAtivo> result = classificadorService.classificarLote(tickers);
        
        // Then
        StepVerifier.create(result)
                .expectNext(TipoAtivo.ACAO_PN)      // PETR4
                .expectNext(TipoAtivo.ACAO_ON)      // VALE3
                .expectNext(TipoAtivo.DESCONHECIDO) // HGLG11 (ambíguo)
                .expectNext(TipoAtivo.ACAO_PN)      // ITUB4
                .expectNext(TipoAtivo.DESCONHECIDO) // BOVA11 (ambíguo)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve retornar Flux vazio para lista null")
    void shouldReturnEmptyFluxForNullList() {
        // When
        Flux<TipoAtivo> result = classificadorService.classificarLote(null);
        
        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve retornar Flux vazio para lista vazia")
    void shouldReturnEmptyFluxForEmptyList() {
        // When
        Flux<TipoAtivo> result = classificadorService.classificarLote(Collections.emptyList());
        
        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve calcular estatísticas corretamente")
    void shouldCalculateStatisticsCorrectly() {
        // Given - Configurar mock para tickers ambíguos
        when(classificadorAtivoPort.classificarPorApi("HGLG11")).thenReturn(Mono.just(TipoAtivo.FII));
        
        // Simular algumas classificações para gerar estatísticas
        classificadorService.classificar("PETR4").block(); // Heurística
        classificadorService.classificar("VALE3").block(); // Heurística
        classificadorService.classificar("HGLG11").block(); // API (ambíguo)
        
        // When
        Map<String, Object> stats = classificadorService.obterEstatisticas();
        
        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.get("totalClassificacoes")).isEqualTo(3);
        assertThat(stats.get("classificacoesHeuristicas")).isEqualTo(2);
        assertThat(stats.get("classificacoesApi")).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve retornar estatísticas iniciais zeradas")
    void shouldReturnInitialZeroStatistics() {
        // When
        Map<String, Object> stats = classificadorService.obterEstatisticas();
        
        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.get("totalClassificacoes")).isEqualTo(0);
        assertThat(stats.get("classificacoesHeuristicas")).isEqualTo(0);
        assertThat(stats.get("classificacoesApi")).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve retornar estatísticas após algumas classificações")
    void shouldReturnStatisticsAfterSomeClassifications() {
        // Given - Simular classificações apenas por heurística
        classificadorService.classificar("PETR4").block();
        classificadorService.classificar("VALE3").block();
        classificadorService.classificar("ITUB4").block();
        
        // When
        Map<String, Object> stats = classificadorService.obterEstatisticas();
        
        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.get("totalClassificacoes")).isEqualTo(3);
        assertThat(stats.get("classificacoesHeuristicas")).isEqualTo(3);
        assertThat(stats.get("classificacoesApi")).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve retornar estatísticas com apenas classificações por heurística")
    void shouldReturnStatisticsWithOnlyHeuristicClassifications() {
        // Given - Simular apenas classificações por heurística
        classificadorService.classificar("PETR4").block();
        classificadorService.classificar("VALE3").block();
        classificadorService.classificar("ITUB4").block();
        classificadorService.classificar("BBDC4").block();
        classificadorService.classificar("ABEV3").block();
        
        // When
        Map<String, Object> stats = classificadorService.obterEstatisticas();
        
        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.get("totalClassificacoes")).isEqualTo(5);
        assertThat(stats.get("classificacoesHeuristicas")).isEqualTo(5);
        assertThat(stats.get("classificacoesApi")).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve retornar estatísticas com apenas classificações por API")
    void shouldReturnStatisticsWithOnlyApiClassifications() {
        // Given - Configurar mocks para tickers ambíguos
        when(classificadorAtivoPort.classificarPorApi("HGLG11")).thenReturn(Mono.just(TipoAtivo.FII));
        when(classificadorAtivoPort.classificarPorApi("XPML11")).thenReturn(Mono.just(TipoAtivo.FII));
        when(classificadorAtivoPort.classificarPorApi("BOVA11")).thenReturn(Mono.just(TipoAtivo.ETF));
        when(classificadorAtivoPort.classificarPorApi("IVVB11")).thenReturn(Mono.just(TipoAtivo.ETF));
        
        // Simular apenas classificações por API (tickers ambíguos)
        classificadorService.classificar("HGLG11").block();
        classificadorService.classificar("XPML11").block();
        classificadorService.classificar("BOVA11").block();
        classificadorService.classificar("IVVB11").block();
        
        // When
        Map<String, Object> stats = classificadorService.obterEstatisticas();
        
        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.get("totalClassificacoes")).isEqualTo(4);
        assertThat(stats.get("classificacoesHeuristicas")).isEqualTo(0);
        assertThat(stats.get("classificacoesApi")).isEqualTo(4);
    }

    @Test
    @DisplayName("Deve retornar estatísticas mistas de heurística e API")
    void shouldReturnMixedStatistics() {
        // Given - Configurar mocks para tickers ambíguos
        when(classificadorAtivoPort.classificarPorApi("HGLG11")).thenReturn(Mono.just(TipoAtivo.FII));
        when(classificadorAtivoPort.classificarPorApi("XPML11")).thenReturn(Mono.just(TipoAtivo.FII));
        
        // Simular classificações mistas
        classificadorService.classificar("PETR4").block(); // Heurística
        classificadorService.classificar("VALE3").block(); // Heurística
        classificadorService.classificar("HGLG11").block(); // API
        classificadorService.classificar("XPML11").block(); // API
        
        // When
        Map<String, Object> stats = classificadorService.obterEstatisticas();
        
        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.get("totalClassificacoes")).isEqualTo(4);
        assertThat(stats.get("classificacoesHeuristicas")).isEqualTo(2);
        assertThat(stats.get("classificacoesApi")).isEqualTo(2);
    }
}