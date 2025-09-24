package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import java.math.BigDecimal;

/**
 * Representação de auditoria para o market cap de um BDR.
 */
public class MarketCap {

    private BigDecimal value;
    private String currency;
    private String quality;
    private String raw;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }
}
