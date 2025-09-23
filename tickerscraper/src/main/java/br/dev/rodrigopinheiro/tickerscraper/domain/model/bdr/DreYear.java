package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import java.math.BigDecimal;

public class DreYear {

    private Integer ano;
    private BigDecimal receitaLiquida;
    private BigDecimal lucroLiquido;
    private BigDecimal ebitda;
    private BigDecimal ebit;
    private BigDecimal margemLiquida;

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public BigDecimal getReceitaLiquida() {
        return receitaLiquida;
    }

    public void setReceitaLiquida(BigDecimal receitaLiquida) {
        this.receitaLiquida = receitaLiquida;
    }

    public BigDecimal getLucroLiquido() {
        return lucroLiquido;
    }

    public void setLucroLiquido(BigDecimal lucroLiquido) {
        this.lucroLiquido = lucroLiquido;
    }

    public BigDecimal getEbitda() {
        return ebitda;
    }

    public void setEbitda(BigDecimal ebitda) {
        this.ebitda = ebitda;
    }

    public BigDecimal getEbit() {
        return ebit;
    }

    public void setEbit(BigDecimal ebit) {
        this.ebit = ebit;
    }

    public BigDecimal getMargemLiquida() {
        return margemLiquida;
    }

    public void setMargemLiquida(BigDecimal margemLiquida) {
        this.margemLiquida = margemLiquida;
    }
}
