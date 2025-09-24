package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import java.math.BigDecimal;

public class HistoricalIndicator {

    private Integer year;
    private BigDecimal pl;
    private BigDecimal pvp;
    private BigDecimal psr;
    private BigDecimal pEbit;
    private BigDecimal pEbitda;
    private BigDecimal pAtivo;
    private BigDecimal roe;
    private BigDecimal roic;
    private BigDecimal roa;
    private BigDecimal margemBruta;
    private BigDecimal margemOperacional;
    private BigDecimal margemLiquida;
    private BigDecimal vpa;
    private BigDecimal lpa;
    private BigDecimal patrimonioPorAtivos;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getPl() {
        return pl;
    }

    public void setPl(BigDecimal pl) {
        this.pl = pl;
    }

    public BigDecimal getPvp() {
        return pvp;
    }

    public void setPvp(BigDecimal pvp) {
        this.pvp = pvp;
    }

    public BigDecimal getPsr() {
        return psr;
    }

    public void setPsr(BigDecimal psr) {
        this.psr = psr;
    }

    public BigDecimal getPEbit() {
        return pEbit;
    }

    public void setPEbit(BigDecimal pEbit) {
        this.pEbit = pEbit;
    }

    public BigDecimal getPEbitda() {
        return pEbitda;
    }

    public void setPEbitda(BigDecimal pEbitda) {
        this.pEbitda = pEbitda;
    }

    public BigDecimal getPAtivo() {
        return pAtivo;
    }

    public void setPAtivo(BigDecimal pAtivo) {
        this.pAtivo = pAtivo;
    }

    public BigDecimal getRoe() {
        return roe;
    }

    public void setRoe(BigDecimal roe) {
        this.roe = roe;
    }

    public BigDecimal getRoic() {
        return roic;
    }

    public void setRoic(BigDecimal roic) {
        this.roic = roic;
    }

    public BigDecimal getRoa() {
        return roa;
    }

    public void setRoa(BigDecimal roa) {
        this.roa = roa;
    }

    public BigDecimal getMargemBruta() {
        return margemBruta;
    }

    public void setMargemBruta(BigDecimal margemBruta) {
        this.margemBruta = margemBruta;
    }

    public BigDecimal getMargemOperacional() {
        return margemOperacional;
    }

    public void setMargemOperacional(BigDecimal margemOperacional) {
        this.margemOperacional = margemOperacional;
    }

    public BigDecimal getMargemLiquida() {
        return margemLiquida;
    }

    public void setMargemLiquida(BigDecimal margemLiquida) {
        this.margemLiquida = margemLiquida;
    }

    public BigDecimal getVpa() {
        return vpa;
    }

    public void setVpa(BigDecimal vpa) {
        this.vpa = vpa;
    }

    public BigDecimal getLpa() {
        return lpa;
    }

    public void setLpa(BigDecimal lpa) {
        this.lpa = lpa;
    }

    public BigDecimal getPatrimonioPorAtivos() {
        return patrimonioPorAtivos;
    }

    public void setPatrimonioPorAtivos(BigDecimal patrimonioPorAtivos) {
        this.patrimonioPorAtivos = patrimonioPorAtivos;
    }
}
