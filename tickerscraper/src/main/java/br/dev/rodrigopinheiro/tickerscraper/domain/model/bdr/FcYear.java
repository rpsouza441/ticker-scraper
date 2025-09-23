package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import java.math.BigDecimal;

public class FcYear {

    private Integer ano;
    private BigDecimal fluxoCaixaOperacional;
    private BigDecimal fluxoCaixaInvestimento;
    private BigDecimal fluxoCaixaFinanciamento;
    private BigDecimal caixaFinal;

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public BigDecimal getFluxoCaixaOperacional() {
        return fluxoCaixaOperacional;
    }

    public void setFluxoCaixaOperacional(BigDecimal fluxoCaixaOperacional) {
        this.fluxoCaixaOperacional = fluxoCaixaOperacional;
    }

    public BigDecimal getFluxoCaixaInvestimento() {
        return fluxoCaixaInvestimento;
    }

    public void setFluxoCaixaInvestimento(BigDecimal fluxoCaixaInvestimento) {
        this.fluxoCaixaInvestimento = fluxoCaixaInvestimento;
    }

    public BigDecimal getFluxoCaixaFinanciamento() {
        return fluxoCaixaFinanciamento;
    }

    public void setFluxoCaixaFinanciamento(BigDecimal fluxoCaixaFinanciamento) {
        this.fluxoCaixaFinanciamento = fluxoCaixaFinanciamento;
    }

    public BigDecimal getCaixaFinal() {
        return caixaFinal;
    }

    public void setCaixaFinal(BigDecimal caixaFinal) {
        this.caixaFinal = caixaFinal;
    }
}
