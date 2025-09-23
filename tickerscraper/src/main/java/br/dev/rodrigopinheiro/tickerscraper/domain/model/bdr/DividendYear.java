package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import java.math.BigDecimal;

public class DividendYear {

    private Integer ano;
    private BigDecimal totalDividendo;
    private BigDecimal dividendYield;

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public BigDecimal getTotalDividendo() {
        return totalDividendo;
    }

    public void setTotalDividendo(BigDecimal totalDividendo) {
        this.totalDividendo = totalDividendo;
    }

    public BigDecimal getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(BigDecimal dividendYield) {
        this.dividendYield = dividendYield;
    }
}
