package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.common.dto;

/**
 * Interface comum para DTOs de informações de cabeçalho.
 * Permite que o scraper genérico trabalhe com diferentes tipos de ativos.
 */
public interface HeaderInfoDTO {
    
    /**
     * Obtém o ticker do ativo.
     * @return O código do ticker
     */
    String ticker();
    
    /**
     * Obtém o nome da empresa/fundo.
     * @return O nome da empresa ou fundo
     */
    String nomeEmpresa();
}