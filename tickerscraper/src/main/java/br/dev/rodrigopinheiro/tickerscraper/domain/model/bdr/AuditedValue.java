package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.Quality;

import java.math.BigDecimal;

/**
 * Representa um valor numérico acompanhado de metadados de auditoria.
 */
public class AuditedValue {

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
