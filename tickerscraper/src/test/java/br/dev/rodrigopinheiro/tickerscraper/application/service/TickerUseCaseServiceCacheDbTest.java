package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.application.port.input.AcaoUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.BdrUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.EtfUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.FiiUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.TickerNotFoundException;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.TipoAtivoResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.BrapiHttpClient;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResponse;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TickerUseCaseService — Cache / DB hit e casos de erro")
class TickerUseCaseServiceCacheDbTest {

    @Mock private TickerClassificationCacheService classificationCache;
    @Mock private TickerDatabaseStrategy databaseStrategy;
    @Mock private BrapiHttpClient brapiClient;
    @Mock private BrapiResponseClassifier brapiClassifier;
    @Mock private AcaoUseCasePort acaoUseCase;
    @Mock private FiiUseCasePort fiiUseCase;
    @Mock private EtfUseCasePort etfUseCase;
    @Mock private BdrUseCasePort bdrUseCase;

    private TickerUseCaseService service;

    @BeforeEach
    void setUp() {
        service = new TickerUseCaseService(
            databaseStrategy,
            brapiClassifier,
            classificationCache,
            brapiClient,
            acaoUseCase,
            fiiUseCase,
            etfUseCase,
            bdrUseCase,
            null, null, null, null
        );
    }

    private Acao mockAcao(String ticker) {
        Acao acao = mock(Acao.class);
        lenient().when(acao.getTicker()).thenReturn(ticker);
        return acao;
    }

    // ===== Entrada =====

    @Test
    @DisplayName("Ticker vazio → erro TickerClassificationException")
    void tickerVazio_deveRetornarErro() {
        StepVerifier.create(service.classificarTicker(" "))
            .expectErrorMatches(ex ->
                ex instanceof br.dev.rodrigopinheiro.tickerscraper.domain.exception.TickerClassificationException
                && ex.getMessage().contains("não pode ser vazio"))
            .verify();
    }

    @Test
    @DisplayName("Ticker null → erro TickerClassificationException")
    void tickerNull_deveRetornarErro() {
        StepVerifier.create(service.classificarTicker(null))
            .expectErrorMatches(ex ->
                ex instanceof br.dev.rodrigopinheiro.tickerscraper.domain.exception.TickerClassificationException)
            .verify();
    }

    // ===== Cache =====

    @Test
    @DisplayName("Cache hit → retorna tipo sem consultar DB ou Brapi")
    void cacheHit_retornaTipoSemRede() {
        when(classificationCache.get("PETR3")).thenReturn(java.util.Optional.of(TipoAtivo.ACAO_ON));

        StepVerifier.create(service.classificarTicker("PETR3"))
            .expectNext(TipoAtivo.ACAO_ON)
            .verifyComplete();

        verify(classificationCache).get("PETR3");
        verifyNoInteractions(databaseStrategy, brapiClient);
    }

    // ===== Database =====

    @Test
    @DisplayName("DB hit (FII) → retorna tipo sem chamar Brapi")
    void dbHitFii_retornaTipoSemBrapi() {
        when(classificationCache.get("HGLG11")).thenReturn(java.util.Optional.empty());
        when(databaseStrategy.verificarTickerNoBanco("HGLG11"))
            .thenReturn(Mono.just(TipoAtivoResult.encontrado(TipoAtivo.FII)));

        StepVerifier.create(service.classificarTicker("HGLG11"))
            .expectNext(TipoAtivo.FII)
            .verifyComplete();

        verify(databaseStrategy).verificarTickerNoBanco("HGLG11");
        verifyNoInteractions(brapiClient);
    }

    @Test
    @DisplayName("DB hit (ACAO_ON) → retorna tipo e salva no cache")
    void dbHitAcao_retornaTipoESalvaNoCache() {
        when(classificationCache.get("PETR3")).thenReturn(java.util.Optional.empty());
        when(databaseStrategy.verificarTickerNoBanco("PETR3"))
            .thenReturn(Mono.just(TipoAtivoResult.encontrado(TipoAtivo.ACAO_ON)));

        StepVerifier.create(service.classificarTicker("PETR3"))
            .expectNext(TipoAtivo.ACAO_ON)
            .verifyComplete();

        verify(classificationCache).put("PETR3", TipoAtivo.ACAO_ON);
    }

    // ===== DB miss → Brapi =====

    @Test
    @DisplayName("DB miss + Brapi não encontrou ticker → TickerClassificationException")
    void dbMiss_brapiNaoEncontrou_retornaErro() {
        when(classificationCache.get("INEXISTENTE")).thenReturn(java.util.Optional.empty());
        when(databaseStrategy.verificarTickerNoBanco("INEXISTENTE"))
            .thenReturn(Mono.just(TipoAtivoResult.naoEncontrado()));
        when(brapiClient.getQuote("INEXISTENTE"))
            .thenReturn(Mono.error(new TickerNotFoundException("INEXISTENTE", "API_BRAPI", "INEXISTENTE", List.of())));

        StepVerifier.create(service.classificarTicker("INEXISTENTE"))
            .expectErrorMatches(ex ->
                ex instanceof br.dev.rodrigopinheiro.tickerscraper.domain.exception.TickerClassificationException)
            .verify();
    }
}