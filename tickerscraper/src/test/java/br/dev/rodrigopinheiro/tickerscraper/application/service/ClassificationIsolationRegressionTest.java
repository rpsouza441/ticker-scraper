package br.dev.rodrigopinheiro.tickerscraper.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.*;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.BrapiHttpClient;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResponse;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.TipoAtivoResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassificationIsolationRegressionTest {
    @Mock TickerDatabaseStrategy databaseStrategy;
    @Mock BrapiResponseClassifier brapiClassifier;
    @Mock TickerClassificationCacheService classificationCache;
    @Mock BrapiHttpClient brapiClient;
    @Mock AcaoUseCasePort acaoUseCase;
    @Mock BdrUseCasePort bdrUseCase;
    @Mock FiiUseCasePort fiiUseCase;
    @Mock EtfUseCasePort etfUseCase;
    @Mock AcaoApiMapper acaoMapper;
    @Mock BdrApiMapper bdrMapper;
    @Mock FiiApiMapper fiiMapper;
    @Mock EtfApiMapper etfMapper;
    @InjectMocks TickerUseCaseService service;
    @Test void coldClassificationDoesNotStartScrapingOrPersistData() {
        var response=new BrapiQuoteResponse(List.of(),"","");
        when(classificationCache.get("SAPR11")).thenReturn(Optional.empty());
        when(databaseStrategy.verificarTickerNoBanco("SAPR11")).thenReturn(Mono.just(TipoAtivoResult.naoEncontrado()));
        when(brapiClient.getQuote("SAPR11")).thenReturn(Mono.just(response));
        when(brapiClassifier.classificarPorResposta(response)).thenReturn(TipoAtivo.UNIT);
        StepVerifier.create(service.classificarTicker("SAPR11")).expectNext(TipoAtivo.UNIT).verifyComplete();
        verifyNoInteractions(acaoUseCase,bdrUseCase,fiiUseCase,etfUseCase);
    }
}
