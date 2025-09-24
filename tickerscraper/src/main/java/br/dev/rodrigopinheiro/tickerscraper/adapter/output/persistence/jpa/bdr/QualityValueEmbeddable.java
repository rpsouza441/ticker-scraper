package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
public class QualityValueEmbeddable {

    @Column(name = "valor", precision = 30, scale = 6)
    private BigDecimal valor;

    @Column(name = "quality")
    private String quality;

    @Column(name = "raw")
    private String raw;
}
