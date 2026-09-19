package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.application.port.input.AcaoUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.BdrUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.EtfUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.FiiUseCasePort;
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
@DisplayName("TickerUseCaseService — DB miss + Brapi sem scraping ACAO")
class TickerUseCaseServiceBrapiAcaoTest {

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

    @Test
    @DisplayName("DB miss → chama Brapi e classifica ACAO sem scraping")
    void dbMiss_chamaBrapi_classificaAcao() {
        String ticker = "NOVO3";
        when(classificationCache.get(ticker)).thenReturn(java.util.Optional.empty());
        when(databaseStrategy.verificarTickerNoBanco(ticker))
            .thenReturn(Mono.just(TipoAtivoResult.naoEncontrado()));
        when(brapiClient.getQuote(ticker)).thenReturn(Mono.just(
            new BrapiQuoteResponse(
                List.of(new BrapiQuoteResult(ticker, "Nova Corp", "Nova Corporation", "BRL", 10.0, "100M", null)),
                null, null
            )
        ));
        when(brapiClassifier.classificarPorResposta(any())).thenReturn(TipoAtivo.ACAO_ON);

        StepVerifier.create(service.classificarTicker(ticker))
            .expectNext(TipoAtivo.ACAO_ON)
            .verifyComplete();

        verify(brapiClient).getQuote(ticker);
        verify(brapiClassifier).classificarPorResposta(any());
        verifyNoInteractions(acaoUseCase, fiiUseCase, etfUseCase, bdrUseCase);
    }

    @Test
    @DisplayName("Ticker novo classificado via Brapi sem scraping ACAO_ON")
    void tickerNovo_classificadoViaBrapiSemScraping() {
        String ticker = "NOVO3";
        when(classificationCache.get(ticker)).thenReturn(java.util.Optional.empty());
        when(databaseStrategy.verificarTickerNoBanco(ticker))
            .thenReturn(Mono.just(TipoAtivoResult.naoEncontrado()));
        when(brapiClient.getQuote(ticker)).thenReturn(Mono.just(
            new BrapiQuoteResponse(
                List.of(new BrapiQuoteResult(ticker, "Nova SA", "Nova Sociedade Anônima", "BRL", 25.0, "500M", null)),
                null, null
            )
        ));
        when(brapiClassifier.classificarPorResposta(any())).thenReturn(TipoAtivo.ACAO_ON);

        StepVerifier.create(service.classificarTicker(ticker))
            .expectNext(TipoAtivo.ACAO_ON)
            .verifyComplete();

        verify(classificationCache).put(ticker, TipoAtivo.ACAO_ON);
        verifyNoInteractions(acaoUseCase, fiiUseCase, etfUseCase, bdrUseCase);
    }
}