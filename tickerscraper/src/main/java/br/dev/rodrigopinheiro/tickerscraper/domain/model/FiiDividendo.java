package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public class FiiDividendo {

    private YearMonth mes;
    private BigDecimal valor;

    public FiiDividendo(YearMonth mes, BigDecimal valor) {
        this.mes = mes;
        this.valor = valor;
    }

    public FiiDividendo() {
    }

    public YearMonth getMes() {
        return mes;
    }

    public void setMes(YearMonth mes) {
        this.mes = mes;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
