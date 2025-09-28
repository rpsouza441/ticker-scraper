package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;

import java.math.BigDecimal;
import java.util.Map;

public record BdrDadosFinanceirosDTO(
        InfoHeader infoHeader,
        InfoCards infoCards,
        InfoSobre infoSobre,
        Map<String, Object> fundamentalIndicators,
        Demonstrativos demonstrativos,
        Map<String, Object> dividendos
) {
    public record InfoHeader(String ticker, String nomeBdr) {}
    public record InfoCards(BigDecimal cotacao, Double variacao12M) {}
    public record InfoSobre(String paridade, String marketCap, String setor, String industria) {}
    public record Demonstrativos(Map<String, Object> dre,
                                 Map<String, Object> bp,
                                 Map<String, Object> fc) {}
}
