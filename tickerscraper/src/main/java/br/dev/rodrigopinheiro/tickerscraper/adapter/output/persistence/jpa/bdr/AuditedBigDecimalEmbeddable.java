package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.converter.QualityAttributeConverter;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.Quality;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Embeddable
public class AuditedBigDecimalEmbeddable {

    @Column(precision = 30, scale = 6)
    private BigDecimal value;

    @Convert(converter = QualityAttributeConverter.class)
    @Column(columnDefinition = "quality_enum")
    private Quality quality = Quality.UNKNOWN;

    @Column(columnDefinition = "TEXT")
    private String raw;
}
