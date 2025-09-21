package br.dev.rodrigopinheiro.tickerscraper.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoAtivoFinanceiroVariavel {
    // Ações
    ACAO_ON("Ação Ordinária"),
    ACAO_PN("Ação Preferencial"), 
    ACAO_PNA("Ação Preferencial Classe A"),
    ACAO_PNB("Ação Preferencial Classe B"),
    ACAO_PNC("Ação Preferencial Classe C"),
    ACAO_PND("Ação Preferencial Classe D"),
    
    // Direitos e Recibos
    DIREITO_SUBSCRICAO_ON("Direito de Subscrição de Ação Ordinária"),
    DIREITO_SUBSCRICAO_PN("Direito de Subscrição de Ação Preferencial"),
    RECIBO_SUBSCRICAO_ON("Recibo de Subscrição de Ação Ordinária"),
    RECIBO_SUBSCRICAO_PN("Recibo de Subscrição de Ação Preferencial"),
    
    // Fundos e ETFs
    FII("Fundo de Investimento Imobiliário"),
    ETF("Exchange Traded Fund"),
    ETF_BDR("ETF BDR (Fundo de Índice Estrangeiro)"),
    UNIT("Unit (Certificado de Depósito de Ações)"),
    
    // BDRs (Brazilian Depositary Receipts)
    BDR_NAO_PATROCINADO("BDR Não Patrocinado (Nível I)"),
    BDR_PATROCINADO("BDR Patrocinado (Nível II/III)"),
    
    DESCONHECIDO("Tipo Desconhecido");
    
    private final String descricao;
    
    /**
     * Classifica ticker baseado no sufixo numérico
     * @param ticker Código do ticker (ex: PETR3, SAPR11)
     * @return Tipo do ativo ou DESCONHECIDO se ambíguo
     */
    public static TipoAtivoFinanceiroVariavel classificarPorSufixo(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            return DESCONHECIDO;
        }
        
        String sufixo = extrairSufixo(ticker.trim().toUpperCase());
        
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
            case "1" -> DIREITO_SUBSCRICAO_ON;
            case "2" -> DIREITO_SUBSCRICAO_PN;
            case "9" -> RECIBO_SUBSCRICAO_ON;
            case "10" -> RECIBO_SUBSCRICAO_PN;
            default -> DESCONHECIDO;
        };
    }
    
    /**
     * Extrai sufixo numérico do ticker
     * @param ticker Código do ticker
     * @return Sufixo numérico como string
     */
    private static String extrairSufixo(String ticker) {
        if (ticker == null || ticker.length() < 2) {
            return "";
        }
        
        // Encontra onde começam os números no final
        int i = ticker.length() - 1;
        while (i >= 0 && Character.isDigit(ticker.charAt(i))) {
            i--;
        }
        
        return i < ticker.length() - 1 ? ticker.substring(i + 1) : "";
    }
    
    /**
     * Verifica se o tipo é uma ação (qualquer classe)
     */
    public boolean isAcao() {
        return this == ACAO_ON || this == ACAO_PN || this == ACAO_PNA || 
               this == ACAO_PNB || this == ACAO_PNC || this == ACAO_PND || this == UNIT;
    }
    
    /**
     * Verifica se o tipo é um BDR (qualquer tipo)
     */
    public boolean isBdr() {
        return this == BDR_NAO_PATROCINADO || this == BDR_PATROCINADO;
    }
}