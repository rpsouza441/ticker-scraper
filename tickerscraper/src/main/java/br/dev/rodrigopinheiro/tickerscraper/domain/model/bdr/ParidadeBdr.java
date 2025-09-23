package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import java.math.BigDecimal;

public class ParidadeBdr {

    private BigDecimal fatorConversao;
    private String tickerOriginal;
    private String bolsaOrigem;
    private String moedaOrigem;

    public BigDecimal getFatorConversao() {
        return fatorConversao;
    }

    public void setFatorConversao(BigDecimal fatorConversao) {
        this.fatorConversao = fatorConversao;
    }

    public String getTickerOriginal() {
        return tickerOriginal;
    }

    public void setTickerOriginal(String tickerOriginal) {
        this.tickerOriginal = tickerOriginal;
    }

    public String getBolsaOrigem() {
        return bolsaOrigem;
    }

    public void setBolsaOrigem(String bolsaOrigem) {
        this.bolsaOrigem = bolsaOrigem;
    }

    public String getMoedaOrigem() {
        return moedaOrigem;
    }

    public void setMoedaOrigem(String moedaOrigem) {
        this.moedaOrigem = moedaOrigem;
    }
}
