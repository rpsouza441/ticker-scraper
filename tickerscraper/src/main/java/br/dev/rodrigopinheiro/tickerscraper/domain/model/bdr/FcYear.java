package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

public class FcYear {

    private Integer ano;
    private QualityValue fluxoCaixaOperacional;
    private QualityValue fluxoCaixaInvestimento;
    private QualityValue fluxoCaixaFinanciamento;

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public QualityValue getFluxoCaixaOperacional() {
        return fluxoCaixaOperacional;
    }

    public void setFluxoCaixaOperacional(QualityValue fluxoCaixaOperacional) {
        this.fluxoCaixaOperacional = fluxoCaixaOperacional;
    }

    public QualityValue getFluxoCaixaInvestimento() {
        return fluxoCaixaInvestimento;
    }

    public void setFluxoCaixaInvestimento(QualityValue fluxoCaixaInvestimento) {
        this.fluxoCaixaInvestimento = fluxoCaixaInvestimento;
    }

    public QualityValue getFluxoCaixaFinanciamento() {
        return fluxoCaixaFinanciamento;
    }

    public void setFluxoCaixaFinanciamento(QualityValue fluxoCaixaFinanciamento) {
        this.fluxoCaixaFinanciamento = fluxoCaixaFinanciamento;
    }
}
