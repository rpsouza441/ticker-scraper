package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import java.math.BigDecimal;

public class BpYear {

    private Integer ano;
    private BigDecimal ativosTotais;
    private BigDecimal passivosTotais;
    private BigDecimal patrimonioLiquido;
    private BigDecimal caixaEDisponibilidades;
    private BigDecimal dividaBruta;

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public BigDecimal getAtivosTotais() {
        return ativosTotais;
    }

    public void setAtivosTotais(BigDecimal ativosTotais) {
        this.ativosTotais = ativosTotais;
    }

    public BigDecimal getPassivosTotais() {
        return passivosTotais;
    }

    public void setPassivosTotais(BigDecimal passivosTotais) {
        this.passivosTotais = passivosTotais;
    }

    public BigDecimal getPatrimonioLiquido() {
        return patrimonioLiquido;
    }

    public void setPatrimonioLiquido(BigDecimal patrimonioLiquido) {
        this.patrimonioLiquido = patrimonioLiquido;
    }

    public BigDecimal getCaixaEDisponibilidades() {
        return caixaEDisponibilidades;
    }

    public void setCaixaEDisponibilidades(BigDecimal caixaEDisponibilidades) {
        this.caixaEDisponibilidades = caixaEDisponibilidades;
    }

    public BigDecimal getDividaBruta() {
        return dividaBruta;
    }

    public void setDividaBruta(BigDecimal dividaBruta) {
        this.dividaBruta = dividaBruta;
    }
}
