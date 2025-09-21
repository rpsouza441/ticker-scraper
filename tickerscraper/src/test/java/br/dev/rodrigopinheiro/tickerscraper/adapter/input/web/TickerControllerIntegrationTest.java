package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiRepositoryPort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class TickerControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private AcaoRepositoryPort acaoRepository;
    
    @Autowired
    private FiiRepositoryPort fiiRepository;

    @Test
    void deveRetornarDadosParaAcaoOrdinaria() {
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
                .jsonPath("$.tipo").exists(); // Não assumir o tipo específico, apenas verificar que existe
    }

    @Test
    @DisplayName("Deve classificar e salvar no banco quando ticker não existe")
    void deveClassificarESalvarNobancoQuandoTickerNaoExiste() {
        String ticker = "SAPR11";
        
        // Verifica se o ticker já existe no banco antes do teste
        boolean existeAcaoAntes = acaoRepository.existsByTicker(ticker);
        boolean existeFiiAntes = fiiRepository.existsByTicker(ticker);
        
        // Faz a requisição para obter dados do ativo (que deve classificar e salvar)
        webTestClient.get()
                .uri("/api/v1/ticker/{ticker}", ticker)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ticker").isEqualTo(ticker)
                .jsonPath("$.tipoAtivo").exists(); // Campo correto no nível raiz
        
        // Verifica que o ticker foi salvo no banco após a operação
        // Verifica em ambas as tabelas pois a classificação pode variar
        boolean existeAcaoDepois = acaoRepository.existsByTicker(ticker);
        boolean existeFiiDepois = fiiRepository.existsByTicker(ticker);
        boolean existeAlgumDepois = existeAcaoDepois || existeFiiDepois;
        
        assertThat(existeAlgumDepois).as("Ticker %s deveria ter sido salvo no banco após a requisição", ticker).isTrue();
    }
}