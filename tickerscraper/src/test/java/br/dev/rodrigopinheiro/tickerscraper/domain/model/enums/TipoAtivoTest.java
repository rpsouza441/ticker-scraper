package br.dev.rodrigopinheiro.tickerscraper.domain.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TipoAtivo - Classificação por Heurística")
class TipoAtivoTest {

    // ===== Sufixo 3 — Ação Ordinária =====

    @ParameterizedTest
    @CsvSource({
        "PETR3, ACAO_ON",
        "VALE3, ACAO_ON",
        "ABEV3, ACAO_ON",
        "MGLU3, ACAO_ON",
        "WEGE3, ACAO_ON",
        "RENT3, ACAO_ON",
        "EZTC3, ACAO_ON",
        "LUPA3, ACAO_ON"
    })
    @DisplayName("Sufixo 3 → ACAO_ON")
    void sufixo3_deveRetornarAcaoOn(String ticker, String esperado) {
        assertThat(TipoAtivo.classificarPorHeuristica(ticker)).isEqualTo(TipoAtivo.valueOf(esperado));
    }

    // ===== Sufixo 4 — Ação Preferencial =====

    @ParameterizedTest
    @CsvSource({
        "PETR4, ACAO_PN",
        "VALE4, ACAO_PN",
        "ITUB4, ACAO_PN",
        "BBDC4, ACAO_PN",
        "ABEV4, ACAO_PN",
        "MGLU4, ACAO_PN"
    })
    @DisplayName("Sufixo 4 → ACAO_PN")
    void sufixo4_deveRetornarAcaoPn(String ticker, String esperado) {
        assertThat(TipoAtivo.classificarPorHeuristica(ticker)).isEqualTo(TipoAtivo.valueOf(esperado));
    }

    // ===== Sufixo 5/6/7/8 — Classes Preferenciais =====

    @ParameterizedTest
    @CsvSource({
        "BBDC5, ACAO_PNA",
        "ITUB5, ACAO_PNA",
        "BBDC6, ACAO_PNB",
        "ITUB6, ACAO_PNB",
        "BBDC7, ACAO_PNC",
        "ITUB7, ACAO_PNC",
        "BBDC8, ACAO_PND",
        "ITUB8, ACAO_PND"
    })
    @DisplayName("Sufixos 5/6/7/8 → Classes Preferenciais")
    void sufixosPreferenciais_devemRetornarClasseCorreta(String ticker, String esperado) {
        assertThat(TipoAtivo.classificarPorHeuristica(ticker)).isEqualTo(TipoAtivo.valueOf(esperado));
    }

    // ===== Sufixo 11 — Ambíguo =====

    @ParameterizedTest
    @ValueSource(strings = {
        "HGLG11", "XPML11", "KNRI11", "MXRF11", "BCFF11", "HGRE11",
        "BOVA11", "IVVB11", "SMAL11", "PIBB11", "ISUS11", "DIVO11",
        "PETR11", "VALE11", "ITUB11", "BBDC11", "ABEV11", "MGLU11"
    })
    @DisplayName("Sufixo 11 → DESCONHECIDO (precisa resolver via DB ou Brapi)")
    void sufixo11_deveRetornarDesconhecido(String ticker) {
        assertThat(TipoAtivo.classificarPorHeuristica(ticker)).isEqualTo(TipoAtivo.DESCONHECIDO);
    }

    // ===== Sufixo 32/33 — BDR Patrocinado =====

    @ParameterizedTest
    @CsvSource({
        "BBDC32, BDR_PATROCINADO",
        "ITUB32, BDR_PATROCINADO",
        "PETR33, BDR_PATROCINADO",
        "VALE33, BDR_PATROCINADO"
    })
    @DisplayName("Sufixo 32/33 → BDR_PATROCINADO")
    void sufixo3233_deveRetornarBdrPatrocinado(String ticker, String esperado) {
        assertThat(TipoAtivo.classificarPorHeuristica(ticker)).isEqualTo(TipoAtivo.valueOf(esperado));
    }

    // ===== Sufixo 34/35 — BDR Não Patrocinado =====

    @ParameterizedTest
    @CsvSource({
        "NVDC34, BDR_NAO_PATROCINADO",
        "MSFT34, BDR_NAO_PATROCINADO",
        "AAPL34, BDR_NAO_PATROCINADO",
        "AMZN35, BDR_NAO_PATROCINADO",
        "GOOG35, BDR_NAO_PATROCINADO"
    })
    @DisplayName("Sufixo 34/35 → BDR_NAO_PATROCINADO")
    void sufixo3435_deveRetornarBdrNaoPatrocinado(String ticker, String esperado) {
        assertThat(TipoAtivo.classificarPorHeuristica(ticker)).isEqualTo(TipoAtivo.valueOf(esperado));
    }

    // ===== Sufixo 9/10 — Recibos de Subscrição =====

    @ParameterizedTest
    @CsvSource({
        "PETR9, RECIBO_SUBSCRICAO_ON",
        "VALE9, RECIBO_SUBSCRICAO_ON",
        "PETR10, RECIBO_SUBSCRICAO_PN",
        "VALE10, RECIBO_SUBSCRICAO_PN"
    })
    @DisplayName("Sufixo 9/10 → Recibos de Subscrição")
    void sufixo910_deveRetornarRecibo(String ticker, String esperado) {
        assertThat(TipoAtivo.classificarPorHeuristica(ticker)).isEqualTo(TipoAtivo.valueOf(esperado));
    }

    // ===== Case insensitivity =====

    @ParameterizedTest
    @ValueSource(strings = {"petr3", "PETR3", "Petr3", "PeTr3"})
    @DisplayName("Deve ser case insensitive")
    void deveSerCaseInsensitive(String ticker) {
        assertThat(TipoAtivo.classificarPorHeuristica(ticker)).isEqualTo(TipoAtivo.ACAO_ON);
    }

    // ===== Espaços =====

    @Test
    @DisplayName("Deve trimar espaços em branco")
    void deveTrimmarEspacos() {
        assertThat(TipoAtivo.classificarPorHeuristica("  PETR3  ")).isEqualTo(TipoAtivo.ACAO_ON);
    }

    // ===== Casos inválidos =====

    @ParameterizedTest
    @ValueSource(strings = {"PETR", "PETR12", "PETR00", "12PETR3", "PETR", ""})
    @DisplayName("Formato inválido → DESCONHECIDO")
    void formatoInvalido_deveRetornarDesconhecido(String ticker) {
        assertThat(TipoAtivo.classificarPorHeuristica(ticker)).isEqualTo(TipoAtivo.DESCONHECIDO);
    }

    @Test
    @DisplayName("null → DESCONHECIDO")
    void null_deveRetornarDesconhecido() {
        assertThat(TipoAtivo.classificarPorHeuristica(null)).isEqualTo(TipoAtivo.DESCONHECIDO);
    }

    // ===== precisaConsultarApi =====

    @ParameterizedTest
    @ValueSource(strings = {"HGLG11", "BOVA11", "XPML11", "PETR11"})
    @DisplayName("Sufixo 11 → precisa consultar API")
    void sufixo11_precisaConsultarApi(String ticker) {
        assertThat(TipoAtivo.precisaConsultarApi(ticker)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"PETR3", "PETR4", "NVDC34", "BBDC32"})
    @DisplayName("Outros sufixos → NÃO precisa consultar API")
    void outrosSufixos_naoPrecisaConsultarApi(String ticker) {
        assertThat(TipoAtivo.precisaConsultarApi(ticker)).isFalse();
    }

    @Test
    @DisplayName("null → NÃO precisa consultar API")
    void null_naoPrecisaConsultarApi() {
        assertThat(TipoAtivo.precisaConsultarApi(null)).isFalse();
    }

    // ===== Helpers de categoria =====

    @Nested
    @DisplayName("Helpers de categoria")
    class HelpersDeCategoria {

        @Test
        void isAcao_deveRetornarTrueParaAcoes() {
            assertThat(TipoAtivo.ACAO_ON.isAcao()).isTrue();
            assertThat(TipoAtivo.ACAO_PN.isAcao()).isTrue();
            assertThat(TipoAtivo.FII.isAcao()).isFalse();
        }

        @Test
        void isFII_deveRetornarTrueParaFII() {
            assertThat(TipoAtivo.FII.isFII()).isTrue();
            assertThat(TipoAtivo.ACAO_ON.isFII()).isFalse();
        }

        @Test
        void isBDR_deveRetornarTrueParaBDRs() {
            assertThat(TipoAtivo.BDR_NAO_PATROCINADO.isBDR()).isTrue();
            assertThat(TipoAtivo.BDR_PATROCINADO.isBDR()).isTrue();
            assertThat(TipoAtivo.ACAO_ON.isBDR()).isFalse();
        }

        @Test
        void isConhecido_deveRetornarFalseParaDesconhecido() {
            assertThat(TipoAtivo.DESCONHECIDO.isConhecido()).isFalse();
            assertThat(TipoAtivo.ACAO_ON.isConhecido()).isTrue();
        }
    }
}
