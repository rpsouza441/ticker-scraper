package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoDividendo;

import java.math.BigDecimal;
import java.time.YearMonth;

public class Dividendo {
    private YearMonth mes;
    private BigDecimal valor;
    private TipoDividendo tipoDividendo;
    private String moeda;

    public Dividendo() {
    }

    public Dividendo(YearMonth mes, BigDecimal valor, TipoDividendo tipoDividendo, String moeda) {
        this.mes = mes;
        this.valor = valor;
        this.tipoDividendo = tipoDividendo;
        this.moeda = moeda;
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

    public TipoDividendo getTipoDividendo() {
        return tipoDividendo;
    }

    public void setTipoDividendo(TipoDividendo tipoDividendo) {
        this.tipoDividendo = tipoDividendo;
    }

    public String getMoeda() {
        return moeda;
    }

    public void setMoeda(String moeda) {
        this.moeda = moeda;
    }
}
