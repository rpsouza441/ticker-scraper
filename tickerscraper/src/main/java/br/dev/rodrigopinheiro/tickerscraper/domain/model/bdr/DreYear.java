package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.Quality;

import java.math.BigDecimal;

public class DreYear {

    private Integer ano;
    private Metric receitaTotalUsd;
    private Metric lucroBrutoUsd;
    private Metric ebitdaUsd;
    private Metric ebitUsd;
    private Metric lucroLiquidoUsd;

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Metric getReceitaTotalUsd() {
        return receitaTotalUsd;
    }

    public void setReceitaTotalUsd(Metric receitaTotalUsd) {
        this.receitaTotalUsd = receitaTotalUsd;
    }

    public Metric getLucroBrutoUsd() {
        return lucroBrutoUsd;
    }

    public void setLucroBrutoUsd(Metric lucroBrutoUsd) {
        this.lucroBrutoUsd = lucroBrutoUsd;
    }

    public Metric getEbitdaUsd() {
        return ebitdaUsd;
    }

    public void setEbitdaUsd(Metric ebitdaUsd) {
        this.ebitdaUsd = ebitdaUsd;
    }

    public Metric getEbitUsd() {
        return ebitUsd;
    }

    public void setEbitUsd(Metric ebitUsd) {
        this.ebitUsd = ebitUsd;
    }

    public Metric getLucroLiquidoUsd() {
        return lucroLiquidoUsd;
    }

    public void setLucroLiquidoUsd(Metric lucroLiquidoUsd) {
        this.lucroLiquidoUsd = lucroLiquidoUsd;
    }

    public static class Metric {

        private BigDecimal value;
        private Quality quality = Quality.UNKNOWN;
        private String raw;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        public Quality getQuality() {
            return quality;
        }

        public void setQuality(Quality quality) {
            this.quality = quality == null ? Quality.UNKNOWN : quality;
        }

        public String getRaw() {
            return raw;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }
    }
}
