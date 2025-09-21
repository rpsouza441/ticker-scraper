package br.dev.rodrigopinheiro.tickerscraper.domain.model.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TipoAtivoFinanceiroVariavelTest {

    @ParameterizedTest
    @CsvSource({
        "PETR3, ACAO_ON",
        "VALE4, ACAO_PN", 
        "SAPR11, DESCONHECIDO",
        "AAPL34, BDR_NAO_PATROCINADO",
        "AURA33, BDR_PATROCINADO",
        "INVALID, DESCONHECIDO"
    })
    void deveClassificarCorretamentePorSufixo(String ticker, TipoAtivoFinanceiroVariavel esperado) {
        var resultado = TipoAtivoFinanceiroVariavel.classificarPorSufixo(ticker);
        assertThat(resultado).isEqualTo(esperado);
    }
    
    @Test
    void deveIdentificarAcoes() {
        assertThat(TipoAtivoFinanceiroVariavel.ACAO_ON.isAcao()).isTrue();
        assertThat(TipoAtivoFinanceiroVariavel.FII.isAcao()).isFalse();
    }
    
    @Test
    void deveIdentificarBdrs() {
        assertThat(TipoAtivoFinanceiroVariavel.BDR_NAO_PATROCINADO.isBdr()).isTrue();
        assertThat(TipoAtivoFinanceiroVariavel.ACAO_ON.isBdr()).isFalse();
    }
}