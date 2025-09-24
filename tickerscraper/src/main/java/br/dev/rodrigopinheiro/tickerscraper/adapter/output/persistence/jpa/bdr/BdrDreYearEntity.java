package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bdr_dre_yearly")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdrDreYearEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bdr_id")
    private BdrEntity bdr;

    @Column(name = "year")
    private Integer ano;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "receita_total_val", precision = 30, scale = 6)),
            @AttributeOverride(name = "quality", column = @Column(name = "receita_total_qual", columnDefinition = "quality_enum")),
            @AttributeOverride(name = "raw", column = @Column(name = "receita_total_raw"))
    })
    private QualityMetricEmbeddable receitaTotalUsd;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "lucro_bruto_val", precision = 30, scale = 6)),
            @AttributeOverride(name = "quality", column = @Column(name = "lucro_bruto_qual", columnDefinition = "quality_enum")),
            @AttributeOverride(name = "raw", column = @Column(name = "lucro_bruto_raw"))
    })
    private QualityMetricEmbeddable lucroBrutoUsd;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "ebitda_val", precision = 30, scale = 6)),
            @AttributeOverride(name = "quality", column = @Column(name = "ebitda_qual", columnDefinition = "quality_enum")),
            @AttributeOverride(name = "raw", column = @Column(name = "ebitda_raw"))
    })
    private QualityMetricEmbeddable ebitdaUsd;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "ebit_val", precision = 30, scale = 6)),
            @AttributeOverride(name = "quality", column = @Column(name = "ebit_qual", columnDefinition = "quality_enum")),
            @AttributeOverride(name = "raw", column = @Column(name = "ebit_raw"))
    })
    private QualityMetricEmbeddable ebitUsd;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "lucro_liquido_val", precision = 30, scale = 6)),
            @AttributeOverride(name = "quality", column = @Column(name = "lucro_liquido_qual", columnDefinition = "quality_enum")),
            @AttributeOverride(name = "raw", column = @Column(name = "lucro_liquido_raw"))
    })
    private QualityMetricEmbeddable lucroLiquidoUsd;
}
