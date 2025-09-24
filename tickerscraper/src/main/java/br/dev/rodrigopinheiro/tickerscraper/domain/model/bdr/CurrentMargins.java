package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import java.math.BigDecimal;
import java.util.Objects;

public class CurrentMargins {

    private BigDecimal margemBruta;
    private BigDecimal margemEbit;
    private BigDecimal margemLiquida;

    public BigDecimal getMargemBruta() {
        return margemBruta;
    }

    public void setMargemBruta(BigDecimal margemBruta) {
        this.margemBruta = margemBruta;
    }

    public BigDecimal getMargemEbit() {
        return margemEbit;
    }

    public void setMargemEbit(BigDecimal margemEbit) {
        this.margemEbit = margemEbit;
    }

    public BigDecimal getMargemLiquida() {
        return margemLiquida;
    }

    public void setMargemLiquida(BigDecimal margemLiquida) {
        this.margemLiquida = margemLiquida;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CurrentMargins that = (CurrentMargins) o;
        return Objects.equals(margemBruta, that.margemBruta)
                && Objects.equals(margemEbit, that.margemEbit)
                && Objects.equals(margemLiquida, that.margemLiquida);
    }

    @Override
    public int hashCode() {
        return Objects.hash(margemBruta, margemEbit, margemLiquida);
    }
}
