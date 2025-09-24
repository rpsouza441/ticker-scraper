package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.converter.QualityAttributeConverter;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.Quality;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
public class QualityValueEmbeddable {

    @Column(name = "value", precision = 30, scale = 6)
    private BigDecimal value;

    @Convert(converter = QualityAttributeConverter.class)
    @Column(name = "quality", columnDefinition = "quality_enum")
    private Quality quality = Quality.UNKNOWN;

    @Column(name = "raw")
    private String raw;
}
