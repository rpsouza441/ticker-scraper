package br.dev.rodrigopinheiro.tickerscraper.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum que representa os tipos de ativos negociados na bolsa brasileira.
 * Inclui métodos de classificação heurística baseados em padrões de ticker.
 */
public enum TipoAtivo {

    // ===== AÇÕES =====
    ACAO_ON             ("ACAO_ON",  "Ação Ordinária",                                  Categoria.ACAO),
    ACAO_PN             ("ACAO_PN",  "Ação Preferencial",                               Categoria.ACAO),
    ACAO_PNA            ("ACAO_PNA", "Ação Preferencial Classe A",                      Categoria.ACAO),
    ACAO_PNB            ("ACAO_PNB", "Ação Preferencial Classe B",                      Categoria.ACAO),
    ACAO_PNC            ("ACAO_PNC", "Ação Preferencial Classe C",                      Categoria.ACAO),
    ACAO_PND            ("ACAO_PND", "Ação Preferencial Classe D",                      Categoria.ACAO),

    // ===== RECIBOS DE SUBSCRIÇÃO =====
    RECIBO_SUBSCRICAO_ON("RECIBO_SUBSCRICAO_ON", "Recibo de Subscrição de Ação Ordinária",    Categoria.RECIBO),
    RECIBO_SUBSCRICAO_PN("RECIBO_SUBSCRICAO_PN", "Recibo de Subscrição de Ação Preferencial", Categoria.RECIBO),

    // ===== FUNDOS/ETFs/UNIT =====
    FII                 ("FII",      "Fundo de Investimento Imobiliário",               Categoria.FII),
    ETF                 ("ETF",      "Fundo de Índice (ETF - Brasil)",                  Categoria.ETF),
    ETF_BDR             ("ETF_BDR",  "Fundo de Índice (ETF BDR - Estrangeiro)",         Categoria.ETF),
    UNIT                ("UNIT",     "Unit (Certificado de Depósito de Ações)",         Categoria.UNIT),

    // ===== BDR =====
    BDR                 ("BDR",      "BDR (Brazilian Depositary Receipt)",              Categoria.BDR),
    BDR_NAO_PATROCINADO ("BDR_NAO_PATROCINADO", "BDR Não Patrocinado (Nível I)",        Categoria.BDR),
    BDR_PATROCINADO     ("BDR_PATROCINADO",     "BDR Patrocinado (Nível II/III)",       Categoria.BDR),

    // ===== OUTROS/DESCONHECIDO =====
    DESCONHECIDO        ("DESCONHECIDO", "Tipo Desconhecido",                           Categoria.OUTRO);
    // ===== Campos =========
    private final String codigo;
    private final String descricao;
    private final Categoria categoria;

    TipoAtivo(String codigo, String descricao, Categoria categoria) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.categoria = categoria;
    }

    // ===== JSON (compatível com versões anteriores) =====
    @JsonValue
    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Categoria getCategoria() {
        return categoria;
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
    @Deprecated
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

        return switch (sufixo) {
            case "3" -> ACAO_ON;
            case "4" -> ACAO_PN;
            case "5" -> ACAO_PNA;
            case "6" -> ACAO_PNB;
            case "7" -> ACAO_PNC;
            case "8" -> ACAO_PND;
            case "11" -> DESCONHECIDO; // FII, ETF ou ETF_BDR - força consulta BD/API
            case "32", "33" -> BDR_PATROCINADO;
            case "34", "35" -> BDR_NAO_PATROCINADO;
            case "9" -> RECIBO_SUBSCRICAO_ON;
            case "10" -> RECIBO_SUBSCRICAO_PN;
            default -> DESCONHECIDO;
        };
    }
    
    /**
     * Verifica se o ticker é ambíguo (final 11) e precisa de consulta externa.
     * 
     * @param ticker o código do ticker
     * @return true se precisa consultar API externa, false caso contrário
     */
    @Deprecated
    public static boolean precisaConsultarApi(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            return false;
        }
        
        String tickerLimpo = ticker.trim().toUpperCase();
        return tickerLimpo.matches("^[A-Z]{4}11$");
    }

    public boolean isAcao()        { return categoria == Categoria.ACAO; }
    public boolean isFII()         { return categoria == Categoria.FII; }
    public boolean isETF()         { return categoria == Categoria.ETF; }
    public boolean isBDR()         { return categoria == Categoria.BDR; }
    public boolean isRecibo()      { return categoria == Categoria.RECIBO; }
    public boolean isUnit()        { return categoria == Categoria.UNIT; }
    public boolean isConhecido()   { return this != DESCONHECIDO; }
    
    @Override
    public String toString() {
        return codigo;
    }
}