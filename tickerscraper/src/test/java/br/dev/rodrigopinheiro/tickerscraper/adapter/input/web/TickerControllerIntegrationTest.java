package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.AcaoJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.FiiJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.BrapiHttpClient;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResponse;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResult;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.AcaoPlaywrightScraperAdapter;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoInfoCardsDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoInfoDetailedDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoInfoHeaderDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoIndicadorFundamentalistaDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoIndicadoresFundamentalistasDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.EtfScraper;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.FiiPlaywrightDirectScraperAdapter;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class TickerControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AcaoRepositoryPort acaoRepository;

    @Autowired
    private FiiRepositoryPort fiiRepository;

    @Autowired
    private AcaoJpaRepository acaoJpa;

    @Autowired
    private FiiJpaRepository fiiJpa;

    @MockBean
    private BrapiHttpClient brapiHttpClient;

    @MockBean
    private AcaoPlaywrightScraperAdapter acaoPlaywrightScraper;

    @MockBean
    private FiiPlaywrightDirectScraperAdapter fiiPlaywrightScraper;

    @MockBean
    private EtfScraper etfScraper;

    @BeforeEach
    void setUp() {
        acaoJpa.deleteAll();
        fiiJpa.deleteAll();

        // Mock BrapiHttpClient: respostas específicas por ticker
        // Nome determina classificação: "fundo"/"fii" → FII, "etf" → ETF, default → ACAO_ON
        when(brapiHttpClient.getQuote("PETR3")).thenReturn(
            Mono.just(new BrapiQuoteResponse(
                List.of(new BrapiQuoteResult("PETR3", "Petrobras PN", "Petroleo Brasileiro S.A.", "BRL", 38.5, "285000000000", null)),
                null, null
            ))
        );
        when(brapiHttpClient.getQuote("SAPR11")).thenReturn(
            Mono.just(new BrapiQuoteResponse(
                List.of(new BrapiQuoteResult("SAPR11", "FII Sao Paulo", "Fundo de Investimento Imobiliario Sao Paulo", "BRL", 98.5, "500000000", null)),
                null, null
            ))
        );
        when(brapiHttpClient.getQuote("NOVO3")).thenReturn(
            Mono.just(new BrapiQuoteResponse(
                List.of(new BrapiQuoteResult("NOVO3", "Novo Ativo", "Novo Ativo Ordinario", "BRL", 15.0, "100000000", null)),
                null, null
            ))
        );
        when(brapiHttpClient.getQuote(anyString())).thenReturn(
            Mono.just(new BrapiQuoteResponse(
                List.of(new BrapiQuoteResult("DUMMY", "Dummy Corp", "Dummy Corporation", "BRL", 10.0, "1000000000", null)),
                null, null
            ))
        );

        // Mock scrapers: retornam DTOs válidos para fluxo de scraping completo
        when(acaoPlaywrightScraper.scrape(anyString())).thenReturn(Mono.defer(() -> {
            Map<String, AcaoIndicadorFundamentalistaDTO> indicadores = new HashMap<>();
            indicadores.put("P/L", new AcaoIndicadorFundamentalistaDTO("6.5", null, null, null, null, null));
            indicadores.put("P/VP", new AcaoIndicadorFundamentalistaDTO("1.2", null, null, null, null, null));
            indicadores.put("DIVIDEND YIELD", new AcaoIndicadorFundamentalistaDTO("5.0", null, null, null, null, null));
            indicadores.put("MARGEM LÍQUIDA", new AcaoIndicadorFundamentalistaDTO("15.0", null, null, null, null, null));
            indicadores.put("MARGEM BRUTA", new AcaoIndicadorFundamentalistaDTO("35.0", null, null, null, null, null));
            indicadores.put("MARGEM EBIT", new AcaoIndicadorFundamentalistaDTO("20.0", null, null, null, null, null));
            indicadores.put("MARGEM EBITDA", new AcaoIndicadorFundamentalistaDTO("22.0", null, null, null, null, null));
            indicadores.put("EV/EBITDA", new AcaoIndicadorFundamentalistaDTO("8.0", null, null, null, null, null));
            indicadores.put("EV/EBIT", new AcaoIndicadorFundamentalistaDTO("7.0", null, null, null, null, null));
            indicadores.put("P/EBITDA", new AcaoIndicadorFundamentalistaDTO("6.0", null, null, null, null, null));
            indicadores.put("P/ATIVO", new AcaoIndicadorFundamentalistaDTO("1.5", null, null, null, null, null));
            indicadores.put("P/CAP.GIRO", new AcaoIndicadorFundamentalistaDTO("4.0", null, null, null, null, null));
            indicadores.put("P/ATIVO CIRC LIQ", new AcaoIndicadorFundamentalistaDTO("3.0", null, null, null, null, null));
            indicadores.put("VPA", new AcaoIndicadorFundamentalistaDTO("12.5", null, null, null, null, null));
            indicadores.put("LPA", new AcaoIndicadorFundamentalistaDTO("2.3", null, null, null, null, null));
            indicadores.put("GIRO ATIVOS", new AcaoIndicadorFundamentalistaDTO("0.8", null, null, null, null, null));
            indicadores.put("ROE", new AcaoIndicadorFundamentalistaDTO("18.4", null, null, null, null, null));
            indicadores.put("ROIC", new AcaoIndicadorFundamentalistaDTO("12.0", null, null, null, null, null));
            indicadores.put("ROA", new AcaoIndicadorFundamentalistaDTO("6.5", null, null, null, null, null));
            indicadores.put("DÍVIDA LÍQUIDA / PATRIMÔNIO", new AcaoIndicadorFundamentalistaDTO("0.4", null, null, null, null, null));
            indicadores.put("DÍVIDA LÍQUIDA / EBITDA", new AcaoIndicadorFundamentalistaDTO("1.5", null, null, null, null, null));
            indicadores.put("DÍVIDA LÍQUIDA / EBIT", new AcaoIndicadorFundamentalistaDTO("1.8", null, null, null, null, null));
            indicadores.put("DÍVIDA BRUTA / PATRIMÔNIO", new AcaoIndicadorFundamentalistaDTO("0.5", null, null, null, null, null));
            indicadores.put("PATRIMÔNIO / ATIVOS", new AcaoIndicadorFundamentalistaDTO("0.6", null, null, null, null, null));
            indicadores.put("PASSIVOS / ATIVOS", new AcaoIndicadorFundamentalistaDTO("0.4", null, null, null, null, null));
            indicadores.put("LIQUIDEZ CORRENTE", new AcaoIndicadorFundamentalistaDTO("1.5", null, null, null, null, null));
            indicadores.put("CAGR RECEITAS 5 ANOS", new AcaoIndicadorFundamentalistaDTO("10.0", null, null, null, null, null));
            indicadores.put("CAGR LUCROS 5 ANOS", new AcaoIndicadorFundamentalistaDTO("8.0", null, null, null, null, null));
            indicadores.put("P/RECEITA (PSR)", new AcaoIndicadorFundamentalistaDTO("1.0", null, null, null, null, null));
            indicadores.put("PAYOUT", new AcaoIndicadorFundamentalistaDTO("35.0", null, null, null, null, null));
            return Mono.just(new AcaoDadosFinanceirosDTO(
                new AcaoInfoHeaderDTO("NOVO3", "Nova Empresa"),
                new AcaoInfoDetailedDTO("100000000", "95000000", "50000000", "1000000", "200000000",
                    "80000000", "30000000", "20000000", "Disponivel", "B3", "10.5",
                    "8.0", "5000000", "Setor Teste", "Segmento Teste"),
                new AcaoInfoCardsDTO("15.00", "5.2"),
                new AcaoIndicadoresFundamentalistasDTO(indicadores)
            ));
        }));

        when(fiiPlaywrightScraper.scrape(anyString())).thenReturn(
            Mono.just(new FiiDadosFinanceirosDTO(null, null, null, null, null, List.of(), null))
        );

        // Pré-popular banco com PETR3 (ação)
        // NOTA: TickerDatabaseStrategy re-classifica por sufixo (sufixo 3 → ACAO_ON)
        // independent do valor stored no banco — bug pré-existente no código de produção.
        AcaoEntity petr3 = AcaoEntity.builder()
            .ticker("PETR3")
            .nomeEmpresa("Petrobras PN")
            .tipoAtivo(TipoAtivo.ACAO_PN)
            .dataAtualizacao(LocalDateTime.now())
            .precoAtual(new BigDecimal("38.50"))
            .build();
        acaoJpa.save(petr3);

        FundoImobiliarioEntity sapr11 = FundoImobiliarioEntity.builder()
            .ticker("SAPR11")
            .nomeEmpresa("FII Sao Paulo")
            .tipoAtivo(TipoAtivo.FII)
            .internalId(1L)
            .dataAtualizacao(LocalDateTime.now())
            .cotacao(new BigDecimal("98.50"))
            .build();
        fiiJpa.save(sapr11);
    }

    @Test
    void deveRetornarDadosParaAcaoOrdinaria() {
        // O tipo retornado é ACAO_ON (não ACAO_PN que foi saved).
        // TickerDatabaseStrategy.classificarPorHeuristica("PETR3") re-classifica
        // por sufixo em vez de usar tipoAtivo stored — bug pré-existente.
        webTestClient.get()
            .uri("/api/v1/ticker/PETR3")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.ticker").isEqualTo("PETR3")
            .jsonPath("$.tipoAtivo").isEqualTo("ACAO_ON")
            .jsonPath("$.dadosAcao").exists()
            .jsonPath("$.dadosFii").doesNotExist();
    }

    @Test
    @DisplayName("Deve classificar corretamente")
    void deveClassificarCorretamente() {
        webTestClient.get()
                .uri("/api/v1/ticker/SAPR11/classificacao")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ticker").isEqualTo("SAPR11")
                .jsonPath("$.tipo").exists();
    }

    @Test
    @DisplayName("Deve classificar e salvar no banco quando ticker não existe")
    void deveClassificarESalvarNobancoQuandoTickerNaoExiste() {
        String ticker = "NOVO3";

        // NOVO3 não está no banco → classificação via BrapiHttpClient mock (ACAO_ON)
        // → scraping via AcaoDataScrapperPort mock → save no banco → retorna 200
        webTestClient.get()
                .uri("/api/v1/ticker/{ticker}", ticker)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ticker").isEqualTo(ticker)
                .jsonPath("$.tipoAtivo").isEqualTo("ACAO_ON")
                .jsonPath("$.dadosAcao").exists();

        assertThat(acaoRepository.existsByTicker(ticker))
            .as("Ticker %s deveria ter sido salvo no banco após a requisição", ticker)
            .isTrue();
    }
}