package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;

/**
 * Value Object que representa o resultado da classificação de um tipo de ativo.
 * 
 * Este objeto encapsula tanto o resultado da operação (se foi encontrado ou não)
 * quanto o tipo de ativo identificado, seguindo os princípios de Domain-Driven Design.
 * 
 * @author Rodrigo Pinheiro
 * @since 1.0
 */
public final class TipoAtivoResult {
    
    private final boolean encontrado;
    private final TipoAtivo tipo;
    
    private TipoAtivoResult(boolean encontrado, TipoAtivo tipo) {
        this.encontrado = encontrado;
        this.tipo = tipo != null ? tipo : TipoAtivo.DESCONHECIDO;
    }
    
    /**
     * Cria um resultado indicando que o tipo foi encontrado.
     * 
     * @param tipo o tipo de ativo identificado (não pode ser null)
     * @return resultado com tipo encontrado
     * @throws IllegalArgumentException se tipo for null
     */
    public static TipoAtivoResult encontrado(TipoAtivo tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo não pode ser null quando encontrado");
        }
        return new TipoAtivoResult(true, tipo);
    }
    
    /**
     * Cria um resultado indicando que o tipo não foi encontrado.
     * 
     * @return resultado com tipo DESCONHECIDO
     */
    public static TipoAtivoResult naoEncontrado() {
        return new TipoAtivoResult(false, TipoAtivo.DESCONHECIDO);
    }
    
    /**
     * Verifica se o tipo foi encontrado.
     * 
     * @return true se o tipo foi identificado, false caso contrário
     */
    public boolean isEncontrado() {
        return encontrado;
    }
    
    /**
     * Obtém o tipo de ativo identificado.
     * 
     * @return o tipo de ativo (nunca null, DESCONHECIDO se não encontrado)
     */
    public TipoAtivo getTipo() {
        return tipo;
    }
    
    /**
     * Verifica se o resultado representa um tipo desconhecido.
     * 
     * @return true se o tipo é DESCONHECIDO
     */
    public boolean isDesconhecido() {
        return tipo == TipoAtivo.DESCONHECIDO;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TipoAtivoResult that = (TipoAtivoResult) obj;
        return encontrado == that.encontrado && tipo == that.tipo;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(encontrado, tipo);
    }
    
    @Override
    public String toString() {
        return String.format("TipoAtivoResult{encontrado=%s, tipo=%s}", encontrado, tipo);
    }
}