package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import java.math.BigDecimal;

public class CurrentIndicators {

    private BigDecimal ultimoPreco;
    private BigDecimal variacaoPercentualDia;
    private BigDecimal variacaoPercentualMes;
    private BigDecimal variacaoPercentualAno;
    private BigDecimal dividendYield;
    private BigDecimal precoLucro;
    private BigDecimal precoValorPatrimonial;
    private BigDecimal valorMercado;
    private BigDecimal volumeMedio;

    public BigDecimal getUltimoPreco() {
        return ultimoPreco;
    }

    public void setUltimoPreco(BigDecimal ultimoPreco) {
        this.ultimoPreco = ultimoPreco;
    }

    public BigDecimal getVariacaoPercentualDia() {
        return variacaoPercentualDia;
    }

    public void setVariacaoPercentualDia(BigDecimal variacaoPercentualDia) {
        this.variacaoPercentualDia = variacaoPercentualDia;
    }

    public BigDecimal getVariacaoPercentualMes() {
        return variacaoPercentualMes;
    }

    public void setVariacaoPercentualMes(BigDecimal variacaoPercentualMes) {
        this.variacaoPercentualMes = variacaoPercentualMes;
    }

    public BigDecimal getVariacaoPercentualAno() {
        return variacaoPercentualAno;
    }

    public void setVariacaoPercentualAno(BigDecimal variacaoPercentualAno) {
        this.variacaoPercentualAno = variacaoPercentualAno;
    }

    public BigDecimal getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(BigDecimal dividendYield) {
        this.dividendYield = dividendYield;
    }

    public BigDecimal getPrecoLucro() {
        return precoLucro;
    }

    public void setPrecoLucro(BigDecimal precoLucro) {
        this.precoLucro = precoLucro;
    }

    public BigDecimal getPrecoValorPatrimonial() {
        return precoValorPatrimonial;
    }

    public void setPrecoValorPatrimonial(BigDecimal precoValorPatrimonial) {
        this.precoValorPatrimonial = precoValorPatrimonial;
    }

    public BigDecimal getValorMercado() {
        return valorMercado;
    }

    public void setValorMercado(BigDecimal valorMercado) {
        this.valorMercado = valorMercado;
    }

    public BigDecimal getVolumeMedio() {
        return volumeMedio;
    }

    public void setVolumeMedio(BigDecimal volumeMedio) {
        this.volumeMedio = volumeMedio;
    }
}
