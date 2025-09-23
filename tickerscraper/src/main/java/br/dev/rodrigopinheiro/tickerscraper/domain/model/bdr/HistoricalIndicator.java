package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import java.math.BigDecimal;

public class HistoricalIndicator {

    private String nomeIndicador;
    private Integer ano;
    private BigDecimal valor;

    public String getNomeIndicador() {
        return nomeIndicador;
    }

    public void setNomeIndicador(String nomeIndicador) {
        this.nomeIndicador = nomeIndicador;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
