package br.dev.rodrigopinheiro.tickerscraper.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum que representa os tipos de ativos negociados na bolsa brasileira.
 * Inclui métodos de classificação heurística baseados em padrões de ticker.
 */
public enum TipoAtivo {
    
    // Ações Específicas
    ACAO_ON("ACAO_ON", "Ação Ordinária", "Ação que confere ao acionista o direito de voto em assembleias e participação nos resultados da empresa."),
    ACAO_PN("ACAO_PN", "Ação Preferencial", "Ação que dá prioridade no recebimento de dividendos, porém geralmente sem direito a voto."),
    ACAO_PNA("ACAO_PNA", "Ação Preferencial Classe A", "Ação preferencial com prioridade sobre outras classes."),
    ACAO_PNB("ACAO_PNB", "Ação Preferencial Classe B", "Ação preferencial com prioridade inferior à classe A."),
    ACAO_PNC("ACAO_PNC", "Ação Preferencial Classe C", "Ação preferencial com menor prioridade, mas com garantia de retorno financeiro."),
    ACAO_PND("ACAO_PND", "Ação Preferencial Classe D", "Ação preferencial com menos direitos que outras classes."),
    ACAO_UNIT("ACAO_UNIT", "Unit", "Pacote de ativos que combina ações ordinárias e preferenciais de uma empresa."),
    
    // Fundos e ETFs
    FII("FII", "Fundo de Investimento Imobiliário", "Fundo que investe em empreendimentos imobiliários, permitindo que investidores participem do mercado imobiliário sem adquirir imóveis diretamente."),
    ETF("ETF", "Fundo de Índice", "Fundo que busca replicar o desempenho de um índice de mercado, permitindo diversificação com a compra de uma única cota."),
    BDR("BDR", "Brazilian Depositary Receipt", "Certificado que representa ações de empresas estrangeiras, permitindo que investidores brasileiros invistam em companhias internacionais."),
        
    // Tipo desconhecido
    DESCONHECIDO("DESCONHECIDO", "Tipo Desconhecido", "Tipo não identificado");
    
    private final String codigo;
    private final String nome;
    private final String descricao;
    
    TipoAtivo(String codigo, String nome, String descricao) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
    }
    
    @JsonValue
    public String getCodigo() {
        return codigo;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    @JsonCreator
    public static TipoAtivo fromCodigo(String codigo) {
        if (codigo == null) {
            return DESCONHECIDO;
        }
        
        for (TipoAtivo tipo : values()) {
            if (tipo.codigo.equalsIgnoreCase(codigo)) {
                return tipo;
            }
        }
        return DESCONHECIDO;
    }
    
    /**
     * Classifica o tipo de ativo baseado em heurística do ticker.
     * 
     * Regras:
     * - XXXX3 → ACAO_ON (Ação Ordinária)
     * - XXXX4 → ACAO_PN (Ação Preferencial)
     * - XXXX5 → ACAO_PNA (Ação Preferencial Classe A)
     * - XXXX6 → ACAO_PNB (Ação Preferencial Classe B)
     * - XXXX7 → ACAO_PNC (Ação Preferencial Classe C)
     * - XXXX8 → ACAO_PND (Ação Preferencial Classe D)
     * - XXXX11 → Ambíguo (pode ser FII, ETF, UNIT ou BDR) - precisa consultar API
     * - Outros padrões → DESCONHECIDO
     * 
     * @param ticker o código do ticker (ex: PETR4, HGLG11)
     * @return o tipo classificado ou DESCONHECIDO se não conseguir determinar
     */
    public static TipoAtivo classificarPorHeuristica(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            return DESCONHECIDO;
        }
        
        String tickerLimpo = ticker.trim().toUpperCase();
        
        // Validar formato básico (4 letras + números)
        if (!tickerLimpo.matches("^[A-Z]{4}\\d+$")) {
            return DESCONHECIDO;
        }
        
        // Extrair sufixo numérico
        String sufixo = tickerLimpo.replaceAll("^[A-Z]{4}", "");
        
        switch (sufixo) {
            case "3":
                return ACAO_ON;  // Ação Ordinária
                
            case "4":
                return ACAO_PN;  // Ação Preferencial
                
            case "5":
                return ACAO_PNA; // Ação Preferencial Classe A
                
            case "6":
                return ACAO_PNB; // Ação Preferencial Classe B
                
            case "7":
                return ACAO_PNC; // Ação Preferencial Classe C
                
            case "8":
                return ACAO_PND; // Ação Preferencial Classe D
                
            case "11":
                // Ambíguo - pode ser FII, ETF, UNIT ou BDR
                // Precisa consultar API externa para determinar
                return DESCONHECIDO;
                
            default:
                return DESCONHECIDO;
        }
    }
    
    /**
     * Verifica se o ticker é ambíguo (final 11) e precisa de consulta externa.
     * 
     * @param ticker o código do ticker
     * @return true se precisa consultar API externa, false caso contrário
     */
    public static boolean precisaConsultarApi(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            return false;
        }
        
        String tickerLimpo = ticker.trim().toUpperCase();
        return tickerLimpo.matches("^[A-Z]{4}11$");
    }
    
    /**
     * Verifica se é uma ação (qualquer tipo de ação).
     */
    public boolean isAcao() {
        return this == ACAO_ON || this == ACAO_PN || this == ACAO_PNA || 
               this == ACAO_PNB || this == ACAO_PNC || this == ACAO_PND || 
               this == ACAO_UNIT;
    }
    
    /**
     * Verifica se é uma ação ordinária.
     */
    public boolean isAcaoOrdinaria() {
        return this == ACAO_ON;
    }
    
    /**
     * Verifica se é uma ação preferencial (qualquer classe).
     */
    public boolean isAcaoPreferencial() {
        return this == ACAO_PN || this == ACAO_PNA || this == ACAO_PNB || 
               this == ACAO_PNC || this == ACAO_PND;
    }
    
    /**
     * Verifica se é um fundo (FII ou ETF).
     */
    public boolean isFundo() {
        return this == FII || this == ETF;
    }
    
    /**
     * Verifica se é um BDR.
     */
    public boolean isBdr() {
        return this == BDR;
    }
    
    /**
     * Verifica se é um tipo conhecido (não DESCONHECIDO).
     */
    public boolean isConhecido() {
        return this != DESCONHECIDO;
    }
    
    @Override
    public String toString() {
        return codigo;
    }
}