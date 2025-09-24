package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.converter.QualityAttributeConverter;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.Quality;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class QualityMetricEmbeddable {

    @Column(name = "value", precision = 30, scale = 6)
    private BigDecimal value;

    @Convert(converter = QualityAttributeConverter.class)
    @Column(name = "quality", columnDefinition = "quality_enum")
    private Quality quality = Quality.UNKNOWN;

    @Column(name = "raw")
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
