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
@DisplayName("TickerUseCaseService — DB miss + Brapi sem scraping BDR")
class TickerUseCaseServiceBrapiBdrTest {

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
    @DisplayName("Ticker novo classificado via Brapi sem scraping BDR")
    void tickerNovo_classificadoViaBrapiSemScrapingBdr() {
        String ticker = "MSFT34";
        when(classificationCache.get(ticker)).thenReturn(java.util.Optional.empty());
        when(databaseStrategy.verificarTickerNoBanco(ticker))
            .thenReturn(Mono.just(TipoAtivoResult.naoEncontrado()));
        when(brapiClient.getQuote(ticker)).thenReturn(Mono.just(
            new BrapiQuoteResponse(
                List.of(new BrapiQuoteResult(ticker, "Microsoft BDR", "Microsoft Corporation BDR", "BRL", 350.0, "1T", null)),
                null, null
            )
        ));
        when(brapiClassifier.classificarPorResposta(any())).thenReturn(TipoAtivo.BDR_NAO_PATROCINADO);

        StepVerifier.create(service.classificarTicker(ticker))
            .expectNext(TipoAtivo.BDR_NAO_PATROCINADO)
            .verifyComplete();

        verifyNoInteractions(acaoUseCase, fiiUseCase, etfUseCase, bdrUseCase);
    }
}