package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.EtfRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.TipoAtivoResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TickerDatabaseStrategy - Busca no Banco")
class TickerDatabaseStrategyTest {

    @Mock private AcaoRepositoryPort acaoRepository;
    @Mock private FiiRepositoryPort fiiRepository;
    @Mock private EtfRepositoryPort etfRepository;

    private TickerDatabaseStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new TickerDatabaseStrategy(acaoRepository, fiiRepository, etfRepository);
    }

    @Nested
    @DisplayName("verificarTickerNoBanco")
    class VerificarTickerNoBanco {

        @Test
        @DisplayName("Ticker encontrado no FII → retorna FII")
        void tickerNoFii_retornaFii() {
            when(fiiRepository.existsByTicker("HGLG11")).thenReturn(true);
            when(acaoRepository.existsByTicker("HGLG11")).thenReturn(false);
            when(etfRepository.existsByTicker("HGLG11")).thenReturn(false);

            StepVerifier.create(strategy.verificarTickerNoBanco("HGLG11"))
                .expectNextMatches(result ->
                    result.isEncontrado() && result.getTipo() == TipoAtivo.FII)
                .verifyComplete();
        }

        @Test
        @DisplayName("Ticker encontrado no ETF → retorna ETF")
        void tickerNoEtf_retornaEtf() {
            when(etfRepository.existsByTicker("BOVA11")).thenReturn(true);
            when(acaoRepository.existsByTicker("BOVA11")).thenReturn(false);
            when(fiiRepository.existsByTicker("BOVA11")).thenReturn(false);

            StepVerifier.create(strategy.verificarTickerNoBanco("BOVA11"))
                .expectNextMatches(result ->
                    result.isEncontrado() && result.getTipo() == TipoAtivo.ETF)
                .verifyComplete();
        }

        @Test
        @DisplayName("Ticker encontrado na Ação → usa heurística para classificar por sufixo")
        void tickerNaAcao_retornaTipoPorHeuristica() {
            when(acaoRepository.existsByTicker("PETR3")).thenReturn(true);
            when(fiiRepository.existsByTicker("PETR3")).thenReturn(false);
            when(etfRepository.existsByTicker("PETR3")).thenReturn(false);

            StepVerifier.create(strategy.verificarTickerNoBanco("PETR3"))
                .expectNextMatches(result ->
                    result.isEncontrado() && result.getTipo() == TipoAtivo.ACAO_ON)
                .verifyComplete();
        }

        @Test
        @DisplayName("Ticker encontrado na Ação (sufixo 4) → retorna ACAO_PN via heurística")
        void tickerNaAcaoSufixo4_retornaAcaoPn() {
            when(acaoRepository.existsByTicker("PETR4")).thenReturn(true);
            when(fiiRepository.existsByTicker("PETR4")).thenReturn(false);
            when(etfRepository.existsByTicker("PETR4")).thenReturn(false);

            StepVerifier.create(strategy.verificarTickerNoBanco("PETR4"))
                .expectNextMatches(result ->
                    result.isEncontrado() && result.getTipo() == TipoAtivo.ACAO_PN)
                .verifyComplete();
        }

        @Test
        @DisplayName("Ticker encontrado na Ação (sufixo 34) → retorna BDR via heurística")
        void tickerNaAcaoSufixo34_retornaBdr() {
            when(acaoRepository.existsByTicker("NVDC34")).thenReturn(true);
            when(fiiRepository.existsByTicker("NVDC34")).thenReturn(false);
            when(etfRepository.existsByTicker("NVDC34")).thenReturn(false);

            StepVerifier.create(strategy.verificarTickerNoBanco("NVDC34"))
                .expectNextMatches(result ->
                    result.isEncontrado() && result.getTipo() == TipoAtivo.BDR_NAO_PATROCINADO)
                .verifyComplete();
        }

        @Test
        @DisplayName("Ticker não encontrado em nenhuma tabela → retorna não encontrado")
        void tickerNaoEncontrado_retornaNaoEncontrado() {
            when(acaoRepository.existsByTicker("NOVO3")).thenReturn(false);
            when(fiiRepository.existsByTicker("NOVO3")).thenReturn(false);
            when(etfRepository.existsByTicker("NOVO3")).thenReturn(false);

            StepVerifier.create(strategy.verificarTickerNoBanco("NOVO3"))
                .expectNextMatches(result ->
                    !result.isEncontrado() && result.getTipo() == TipoAtivo.DESCONHECIDO)
                .verifyComplete();
        }

        @Test
        @DisplayName("Prioridade: FII > ETF > Ação")
        void prioridadeFiiSobreEtfEAcao() {
            when(fiiRepository.existsByTicker("DUPLICADO11")).thenReturn(true);
            when(etfRepository.existsByTicker("DUPLICADO11")).thenReturn(true);
            when(acaoRepository.existsByTicker("DUPLICADO11")).thenReturn(true);

            StepVerifier.create(strategy.verificarTickerNoBanco("DUPLICADO11"))
                .expectNextMatches(result ->
                    result.isEncontrado() && result.getTipo() == TipoAtivo.FII)
                .verifyComplete();
        }

        @Test
        @DisplayName("Prioridade: ETF > Ação (quando FII não existe)")
        void prioridadeEtfSobreAcao() {
            when(fiiRepository.existsByTicker("BOVA11")).thenReturn(false);
            when(etfRepository.existsByTicker("BOVA11")).thenReturn(true);
            when(acaoRepository.existsByTicker("BOVA11")).thenReturn(true);

            StepVerifier.create(strategy.verificarTickerNoBanco("BOVA11"))
                .expectNextMatches(result ->
                    result.isEncontrado() && result.getTipo() == TipoAtivo.ETF)
                .verifyComplete();
        }

        @Test
        @DisplayName("Erro no repository → retorna false (não quebra)")
        void erroNoRepository_retornaFalse() {
            when(fiiRepository.existsByTicker("ERRO")).thenThrow(new RuntimeException("DB error"));
            when(etfRepository.existsByTicker("ERRO")).thenReturn(false);
            when(acaoRepository.existsByTicker("ERRO")).thenReturn(false);

            StepVerifier.create(strategy.verificarTickerNoBanco("ERRO"))
                .expectNextMatches(result -> !result.isEncontrado())
                .verifyComplete();
        }
    }
}
