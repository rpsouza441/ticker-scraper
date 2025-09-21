package br.dev.rodrigopinheiro.tickerscraper.domain.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;


import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TipoAtivo - Testes de Classificação Heurística")
class TipoAtivoTest {

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
        "RENT3, ACAO_ON",
        "SUZB3, ACAO_ON",
        "LREN3, ACAO_ON",
        "GGBR4, ACAO_PN",
        "USIM5, ACAO_PNA",
        "CSNA3, ACAO_ON",
        "GOAU4, ACAO_PN",
        "KLBN4, ACAO_PN",
        "CYRE3, ACAO_ON",
        "MRFG3, ACAO_ON",
        "BEEF3, ACAO_ON",
        "JBSS3, ACAO_ON",
        "BRFS3, ACAO_ON",
        "SMTO3, ACAO_ON",
        "CCRO3, ACAO_ON",
        "EQTL3, ACAO_ON",
        "CSAN3, ACAO_ON",
        "RADL3, ACAO_ON",
        "HAPV3, ACAO_ON",
        "FLRY3, ACAO_ON",
        "QUAL3, ACAO_ON",
        "DXCO3, ACAO_ON",
        "RDOR3, ACAO_ON",
        "PCAR3, ACAO_ON",
        "AZUL4, ACAO_PN",
        "GOLL4, ACAO_PN",
        "EMBR3, ACAO_ON",
        "RAIZ4, ACAO_PN",
        "LWSA3, ACAO_ON",
        "VVAR3, ACAO_ON",
        "LAME4, ACAO_PN",
        "TOTS3, ACAO_ON",
        "VIVT3, ACAO_ON",
        "TIMS3, ACAO_ON",
        "ELET3, ACAO_ON",
        "ELET6, ACAO_PNB",
        "CMIG4, ACAO_PN",
        "CPFE3, ACAO_ON",
        "EGIE3, ACAO_ON",
        "ENGI4, ACAO_PN",
        "TAEE4, ACAO_PN",
        "NEOE3, ACAO_ON",
        "CPLE6, ACAO_PNB",
        "ENEV3, ACAO_ON",
        "TRPL4, ACAO_PN",
        "ENBR3, ACAO_ON",
        "SAPR4, ACAO_PN",
        "SLCE3, ACAO_ON",
        "YDUQ3, ACAO_ON",
        "COGN3, ACAO_ON",
        "SEER3, ACAO_ON",
        "ANIM3, ACAO_ON",
        "VAMO3, ACAO_ON",
        "MULT3, ACAO_ON",
        "IGTI3, ACAO_ON",
        "POSI3, ACAO_ON",
        "CASH3, ACAO_ON",
        "PARC3, ACAO_ON",
        "CARD3, ACAO_ON",
        "SANB4, ACAO_PN",
        "BPAC4, ACAO_PN",
        "PINE4, ACAO_PN",
        "BMGB4, ACAO_PN",
        "BPAN4, ACAO_PN",
        "BRSR6, ACAO_PNB",
        "BEES3, ACAO_ON",
        "BEES4, ACAO_PN",
        "IRBR3, ACAO_ON",
        "SULA4, ACAO_PN",
        "PSSA3, ACAO_ON",
        "BBSE3, ACAO_ON",
        "CIEL3, ACAO_ON",
        "NTCO3, ACAO_ON",
        "MDIA3, ACAO_ON",
        "OIBR3, ACAO_ON",
        "OIBR4, ACAO_PN",
        "TGMA3, ACAO_ON",
        "SHOW3, ACAO_ON",
        "MEAL3, ACAO_ON",
        "IFCM3, ACAO_ON",
        "SOMA3, ACAO_ON",
        "MOVI3, ACAO_ON",
        "EVEN3, ACAO_ON",
        "JHSF3, ACAO_ON",
        "MRVE3, ACAO_ON",
        "GFSA3, ACAO_ON",
        "PDGR3, ACAO_ON",
        "TCSA3, ACAO_ON",
        "HYPE3, ACAO_ON",
        "TEND3, ACAO_ON",
        "PLPL3, ACAO_ON",
        "DIRR3, ACAO_ON",
        "ALSO3, ACAO_ON",
        "ALPA4, ACAO_PN",
        "RECV3, ACAO_ON",
        "RRRP3, ACAO_ON",
        "ARZZ3, ACAO_ON",
        "GRND3, ACAO_ON",
        "FRAS3, ACAO_ON",
        "MLAS3, ACAO_ON",
        "POMO4, ACAO_PN",
        "CAML3, ACAO_ON",
        "JALL3, ACAO_ON",
        "KEPL3, ACAO_ON",
        "LJQQ3, ACAO_ON",
        "MYPK3, ACAO_ON",
        "ONCO3, ACAO_ON",
        "PGMN3, ACAO_ON",
        "PNVL3, ACAO_ON",
        "STBP3, ACAO_ON",
        "VULC3, ACAO_ON",
        "WEST3, ACAO_ON",
        "WIZS3, ACAO_ON"
    })
    @DisplayName("Deve classificar ações corretamente (sufixos 3, 4, 5, 6, 8)")
    void shouldClassifyAcoesCorrectly(String ticker, String expectedType) {
        // When
        TipoAtivo result = TipoAtivo.classificarPorHeuristica(ticker);
        
        // Then
        assertThat(result).isEqualTo(TipoAtivo.valueOf(expectedType));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "HGLG11", "XPML11", "KNRI11", "MXRF11", "BCFF11", "HGRE11", 
        "VISC11", "GGRC11", "KNCR11", "RBRF11", "RBRR11", "RBRY11",
        "BOVA11", "IVVB11", "SMAL11", "PIBB11", "ISUS11", "DIVO11",
        "PETR11", "VALE11", "ITUB11", "BBDC11", "ABEV11", "MGLU11"
    })
    @DisplayName("Deve marcar tickers terminados em 11 como DESCONHECIDO (ambíguos)")
    void shouldMarkTickers11AsDesconhecido(String ticker) {
        // When
        TipoAtivo result = TipoAtivo.classificarPorHeuristica(ticker);
        
        // Then
        assertThat(result).isEqualTo(TipoAtivo.DESCONHECIDO);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "HGLG11", "XPML11", "KNRI11", "MXRF11", "BCFF11", "HGRE11",
        "BOVA11", "IVVB11", "SMAL11", "PIBB11", "ISUS11", "DIVO11",
        "PETR11", "VALE11", "ITUB11", "BBDC11", "ABEV11", "MGLU11"
    })
    @DisplayName("Deve identificar tickers que precisam consultar API")
    void shouldIdentifyTickersThatNeedApiConsultation(String ticker) {
        // When
        boolean needsApi = TipoAtivo.precisaConsultarApi(ticker);
        
        // Then
        assertThat(needsApi).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "PETR3", "PETR4", "VALE3", "VALE5", "ITUB4", "BBDC4", "ABEV3"
    })
    @DisplayName("Deve identificar tickers que NÃO precisam consultar API")
    void shouldIdentifyTickersThatDontNeedApiConsultation(String ticker) {
        // When
        boolean needsApi = TipoAtivo.precisaConsultarApi(ticker);
        
        // Then
        assertThat(needsApi).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "", "   ", "PETR", "PETR123", "12345", "PETR1", "PETR2", "PETR7", "PETR9", "PETR10", "PETR12"
    })
    @DisplayName("Deve retornar DESCONHECIDO para tickers inválidos")
    void shouldReturnDesconhecidoForInvalidTickers(String ticker) {
        // When
        TipoAtivo result = TipoAtivo.classificarPorHeuristica(ticker);
        
        // Then
        assertThat(result).isEqualTo(TipoAtivo.DESCONHECIDO);
    }

    @Test
    @DisplayName("Deve retornar DESCONHECIDO para ticker null")
    void shouldReturnDesconhecidoForNullTicker() {
        // When
        TipoAtivo result = TipoAtivo.classificarPorHeuristica(null);
        
        // Then
        assertThat(result).isEqualTo(TipoAtivo.DESCONHECIDO);
    }

    @Test
    @DisplayName("Deve ser case insensitive")
    void shouldBeCaseInsensitive() {
        // When
        TipoAtivo result1 = TipoAtivo.classificarPorHeuristica("petr4");
        TipoAtivo result2 = TipoAtivo.classificarPorHeuristica("PETR4");
        TipoAtivo result3 = TipoAtivo.classificarPorHeuristica("Petr4");
        
        // Then
        assertThat(result1).isEqualTo(TipoAtivo.ACAO_PN);
        assertThat(result2).isEqualTo(TipoAtivo.ACAO_PN);
        assertThat(result3).isEqualTo(TipoAtivo.ACAO_PN);
    }

    @Test
    @DisplayName("Deve trimar espaços em branco")
    void shouldTrimWhitespace() {
        // When
        TipoAtivo result = TipoAtivo.classificarPorHeuristica("  PETR4  ");
        
        // Then
        assertThat(result).isEqualTo(TipoAtivo.ACAO_PN);
    }

    @Test
    @DisplayName("Deve verificar se é ação")
    void shouldCheckIfIsAcao() {
        // Tipos específicos de ações
        assertThat(TipoAtivo.ACAO_ON.isAcao()).isTrue();
        assertThat(TipoAtivo.ACAO_PN.isAcao()).isTrue();
        assertThat(TipoAtivo.ACAO_PNA.isAcao()).isTrue();
        assertThat(TipoAtivo.ACAO_PNB.isAcao()).isTrue();
        assertThat(TipoAtivo.ACAO_PNC.isAcao()).isTrue();
        assertThat(TipoAtivo.ACAO_PND.isAcao()).isTrue();
        assertThat(TipoAtivo.ACAO_UNIT.isAcao()).isTrue();
        
        // Outros tipos
        assertThat(TipoAtivo.FII.isAcao()).isFalse();
        assertThat(TipoAtivo.ETF.isAcao()).isFalse();
        assertThat(TipoAtivo.BDR.isAcao()).isFalse();
        assertThat(TipoAtivo.DESCONHECIDO.isAcao()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é fundo")
    void shouldCheckIfIsFundo() {
        assertThat(TipoAtivo.FII.isFundo()).isTrue();
        assertThat(TipoAtivo.ETF.isFundo()).isTrue();
        assertThat(TipoAtivo.ACAO_ON.isFundo()).isFalse();
        assertThat(TipoAtivo.ACAO_PN.isFundo()).isFalse();
        assertThat(TipoAtivo.BDR.isFundo()).isFalse();
        assertThat(TipoAtivo.DESCONHECIDO.isFundo()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é conhecido")
    void shouldCheckIfIsConhecido() {
        // Tipos específicos de ações
        assertThat(TipoAtivo.ACAO_ON.isConhecido()).isTrue();
        assertThat(TipoAtivo.ACAO_PN.isConhecido()).isTrue();
        assertThat(TipoAtivo.ACAO_PNA.isConhecido()).isTrue();
        assertThat(TipoAtivo.ACAO_PNB.isConhecido()).isTrue();
        assertThat(TipoAtivo.ACAO_PNC.isConhecido()).isTrue();
        assertThat(TipoAtivo.ACAO_PND.isConhecido()).isTrue();
        assertThat(TipoAtivo.ACAO_UNIT.isConhecido()).isTrue();
        
        // Outros tipos
        assertThat(TipoAtivo.FII.isConhecido()).isTrue();
        assertThat(TipoAtivo.ETF.isConhecido()).isTrue();
        assertThat(TipoAtivo.BDR.isConhecido()).isTrue();
        
        // Tipo desconhecido
        assertThat(TipoAtivo.DESCONHECIDO.isConhecido()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é ação ordinária")
    void shouldCheckIfIsAcaoOrdinaria() {
        // Ações ordinárias
        assertThat(TipoAtivo.ACAO_ON.isAcaoOrdinaria()).isTrue();
        
        // Ações preferenciais
        assertThat(TipoAtivo.ACAO_PN.isAcaoOrdinaria()).isFalse();
        assertThat(TipoAtivo.ACAO_PNA.isAcaoOrdinaria()).isFalse();
        assertThat(TipoAtivo.ACAO_PNB.isAcaoOrdinaria()).isFalse();
        assertThat(TipoAtivo.ACAO_PNC.isAcaoOrdinaria()).isFalse();
        assertThat(TipoAtivo.ACAO_PND.isAcaoOrdinaria()).isFalse();
        
        // Outros tipos
        assertThat(TipoAtivo.ACAO_UNIT.isAcaoOrdinaria()).isFalse();
        assertThat(TipoAtivo.BDR.isAcaoOrdinaria()).isFalse();
        assertThat(TipoAtivo.FII.isAcaoOrdinaria()).isFalse();
        assertThat(TipoAtivo.ETF.isAcaoOrdinaria()).isFalse();
        assertThat(TipoAtivo.DESCONHECIDO.isAcaoOrdinaria()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é ação preferencial")
    void shouldCheckIfIsAcaoPreferencial() {
        // Ações preferenciais
        assertThat(TipoAtivo.ACAO_PN.isAcaoPreferencial()).isTrue();
        assertThat(TipoAtivo.ACAO_PNA.isAcaoPreferencial()).isTrue();
        assertThat(TipoAtivo.ACAO_PNB.isAcaoPreferencial()).isTrue();
        assertThat(TipoAtivo.ACAO_PNC.isAcaoPreferencial()).isTrue();
        assertThat(TipoAtivo.ACAO_PND.isAcaoPreferencial()).isTrue();
        
        // Ações ordinárias
        assertThat(TipoAtivo.ACAO_ON.isAcaoPreferencial()).isFalse();
        
        // Outros tipos
        assertThat(TipoAtivo.ACAO_UNIT.isAcaoPreferencial()).isFalse();
        assertThat(TipoAtivo.BDR.isAcaoPreferencial()).isFalse();
        assertThat(TipoAtivo.FII.isAcaoPreferencial()).isFalse();
        assertThat(TipoAtivo.ETF.isAcaoPreferencial()).isFalse();
        assertThat(TipoAtivo.DESCONHECIDO.isAcaoPreferencial()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é BDR")
    void shouldCheckIfIsBdr() {
        // BDR
        assertThat(TipoAtivo.BDR.isBdr()).isTrue();
        
        // Outros tipos
        assertThat(TipoAtivo.ACAO_ON.isBdr()).isFalse();
        assertThat(TipoAtivo.ACAO_PN.isBdr()).isFalse();
        assertThat(TipoAtivo.ACAO_PNA.isBdr()).isFalse();
        assertThat(TipoAtivo.ACAO_PNB.isBdr()).isFalse();
        assertThat(TipoAtivo.ACAO_PNC.isBdr()).isFalse();
        assertThat(TipoAtivo.ACAO_PND.isBdr()).isFalse();
        assertThat(TipoAtivo.ACAO_UNIT.isBdr()).isFalse();
        assertThat(TipoAtivo.FII.isBdr()).isFalse();
        assertThat(TipoAtivo.ETF.isBdr()).isFalse();
        assertThat(TipoAtivo.DESCONHECIDO.isBdr()).isFalse();
    }

    @Test
    @DisplayName("Deve serializar/deserializar JSON corretamente")
    void shouldSerializeDeserializeJsonCorrectly() {
        // Given
        String codigo = "ACAO_ON";
        
        // When
        TipoAtivo tipo = TipoAtivo.fromCodigo(codigo);
        String serialized = tipo.getCodigo();
        
        // Then
        assertThat(tipo).isEqualTo(TipoAtivo.ACAO_ON);
        assertThat(serialized).isEqualTo("ACAO_ON");
    }

    @Test
    @DisplayName("Deve retornar DESCONHECIDO para código inválido")
    void shouldReturnDesconhecidoForInvalidCode() {
        // When
        TipoAtivo result = TipoAtivo.fromCodigo("INVALID");
        
        // Then
        assertThat(result).isEqualTo(TipoAtivo.DESCONHECIDO);
    }

    @Test
    @DisplayName("Deve retornar DESCONHECIDO para código null")
    void shouldReturnDesconhecidoForNullCode() {
        // When
        TipoAtivo result = TipoAtivo.fromCodigo(null);
        
        // Then
        assertThat(result).isEqualTo(TipoAtivo.DESCONHECIDO);
    }
}