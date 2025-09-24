package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
public class BdrCurrentMarginsEmbeddable {

    @Column(name = "margem_bruta", precision = 19, scale = 6)
    private BigDecimal margemBruta;

    @Column(name = "margem_operacional", precision = 19, scale = 6)
    private BigDecimal margemOperacional;

    @Column(name = "margem_liquida", precision = 19, scale = 6)
    private BigDecimal margemLiquida;
}
