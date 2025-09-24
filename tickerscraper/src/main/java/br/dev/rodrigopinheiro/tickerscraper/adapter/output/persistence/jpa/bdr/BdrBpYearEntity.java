package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bdr_bp_yearly")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdrBpYearEntity {

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
            @AttributeOverride(name = "value", column = @Column(name = "ativos_totais_val", precision = 30, scale = 6)),
            @AttributeOverride(name = "quality", column = @Column(name = "ativos_totais_qual", columnDefinition = "quality_enum")),
            @AttributeOverride(name = "raw", column = @Column(name = "ativos_totais_raw", columnDefinition = "TEXT"))
    })
    private AuditedBigDecimalEmbeddable ativosTotais;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "passivos_totais_val", precision = 30, scale = 6)),
            @AttributeOverride(name = "quality", column = @Column(name = "passivos_totais_qual", columnDefinition = "quality_enum")),
            @AttributeOverride(name = "raw", column = @Column(name = "passivos_totais_raw", columnDefinition = "TEXT"))
    })
    private AuditedBigDecimalEmbeddable passivosTotais;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "divida_lp_val", precision = 30, scale = 6)),
            @AttributeOverride(name = "quality", column = @Column(name = "divida_lp_qual", columnDefinition = "quality_enum")),
            @AttributeOverride(name = "raw", column = @Column(name = "divida_lp_raw", columnDefinition = "TEXT"))
    })
    private AuditedBigDecimalEmbeddable dividaLongoPrazo;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "pl_val", precision = 30, scale = 6)),
            @AttributeOverride(name = "quality", column = @Column(name = "pl_qual", columnDefinition = "quality_enum")),
            @AttributeOverride(name = "raw", column = @Column(name = "pl_raw", columnDefinition = "TEXT"))
    })
    private AuditedBigDecimalEmbeddable pl;
}
