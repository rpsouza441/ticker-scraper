package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Embeddable
public class AuditedBigDecimalEmbeddable {

    @Column(precision = 19, scale = 2)
    private BigDecimal value;

    @Column(length = 50)
    private String quality;

    @Column(columnDefinition = "TEXT")
    private String raw;
}
