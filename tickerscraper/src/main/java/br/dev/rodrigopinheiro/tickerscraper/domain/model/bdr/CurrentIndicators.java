package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import java.math.BigDecimal;
import java.util.Objects;

public class CurrentIndicators {

    private BigDecimal pl;
    private BigDecimal pvp;
    private BigDecimal psr;
    private BigDecimal pEbit;
    private BigDecimal pEbitda;
    private BigDecimal pAtivo;
    private BigDecimal roe;
    private BigDecimal roic;
    private BigDecimal roa;
    private CurrentMargins margens;
    private BigDecimal vpa;
    private BigDecimal lpa;
    private BigDecimal patrimonioPorAtivos;

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

    public CurrentMargins getMargens() {
        return margens;
    }

    public void setMargens(CurrentMargins margens) {
        this.margens = margens;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CurrentIndicators that = (CurrentIndicators) o;
        return Objects.equals(pl, that.pl)
                && Objects.equals(pvp, that.pvp)
                && Objects.equals(psr, that.psr)
                && Objects.equals(pEbit, that.pEbit)
                && Objects.equals(pEbitda, that.pEbitda)
                && Objects.equals(pAtivo, that.pAtivo)
                && Objects.equals(roe, that.roe)
                && Objects.equals(roic, that.roic)
                && Objects.equals(roa, that.roa)
                && Objects.equals(margens, that.margens)
                && Objects.equals(vpa, that.vpa)
                && Objects.equals(lpa, that.lpa)
                && Objects.equals(patrimonioPorAtivos, that.patrimonioPorAtivos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pl, pvp, psr, pEbit, pEbitda, pAtivo, roe, roic, roa, margens, vpa, lpa, patrimonioPorAtivos);
    }
}
