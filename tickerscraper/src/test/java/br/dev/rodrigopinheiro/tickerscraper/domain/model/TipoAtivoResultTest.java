package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivoFinanceiroVariavel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TipoAtivoResult - Value Object Tests")
class TipoAtivoResultTest {

    @Test
    @DisplayName("Deve criar resultado encontrado com tipo válido")
    void deveCriarResultadoEncontrado() {
        // When
        var resultado = TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.ACAO_ON);
        
        // Then
        assertThat(resultado.isEncontrado()).isTrue();
        assertThat(resultado.getTipo()).isEqualTo(TipoAtivoFinanceiroVariavel.ACAO_ON);
        assertThat(resultado.isDesconhecido()).isFalse();
    }
    
    @Test
    @DisplayName("Deve criar resultado não encontrado")
    void deveCriarResultadoNaoEncontrado() {
        // When
        var resultado = TipoAtivoResult.naoEncontrado();
        
        // Then
        assertThat(resultado.isEncontrado()).isFalse();
        assertThat(resultado.getTipo()).isEqualTo(TipoAtivoFinanceiroVariavel.DESCONHECIDO);
        assertThat(resultado.isDesconhecido()).isTrue();
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar resultado encontrado com tipo null")
    void deveLancarExcecaoParaTipoNull() {
        // When/Then
        assertThatThrownBy(() -> TipoAtivoResult.encontrado(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Tipo não pode ser null quando encontrado");
    }
    
    @Test
    @DisplayName("Deve verificar igualdade corretamente")
    void deveVerificarIgualdade() {
        // Given
        var resultado1 = TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.FII);
        var resultado2 = TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.FII);
        var resultado3 = TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.ACAO_ON);
        var resultado4 = TipoAtivoResult.naoEncontrado();
        
        // Then
        assertThat(resultado1).isEqualTo(resultado2);
        assertThat(resultado1).isNotEqualTo(resultado3);
        assertThat(resultado1).isNotEqualTo(resultado4);
        assertThat(resultado1).isNotEqualTo(null);
        assertThat(resultado1).isNotEqualTo("string");
    }
    
    @Test
    @DisplayName("Deve ter hashCode consistente")
    void deveTerHashCodeConsistente() {
        // Given
        var resultado1 = TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.ETF);
        var resultado2 = TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.ETF);
        
        // Then
        assertThat(resultado1.hashCode()).isEqualTo(resultado2.hashCode());
    }
    
    @Test
    @DisplayName("Deve ter toString informativo")
    void deveTerToStringInformativo() {
        // Given
        var resultadoEncontrado = TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.BDR_PATROCINADO);
        var resultadoNaoEncontrado = TipoAtivoResult.naoEncontrado();
        
        // Then
        assertThat(resultadoEncontrado.toString())
            .contains("encontrado=true")
            .contains("BDR_PATROCINADO");
            
        assertThat(resultadoNaoEncontrado.toString())
            .contains("encontrado=false")
            .contains("DESCONHECIDO");
    }
    
    @Test
    @DisplayName("Deve identificar tipos desconhecidos corretamente")
    void deveIdentificarTiposDesconhecidos() {
        // Given
        var resultadoDesconhecido = TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.DESCONHECIDO);
        var resultadoConhecido = TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.ACAO_PN);
        var resultadoNaoEncontrado = TipoAtivoResult.naoEncontrado();
        
        // Then
        assertThat(resultadoDesconhecido.isDesconhecido()).isTrue();
        assertThat(resultadoConhecido.isDesconhecido()).isFalse();
        assertThat(resultadoNaoEncontrado.isDesconhecido()).isTrue();
    }
    
    @Test
    @DisplayName("Deve ser imutável")
    void deveSerImutavel() {
        // Given
        var resultado = TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.UNIT);
        
        // Then - não deve ter métodos setters públicos
        assertThat(resultado.getClass().getDeclaredMethods())
            .noneMatch(method -> method.getName().startsWith("set"));
    }
}