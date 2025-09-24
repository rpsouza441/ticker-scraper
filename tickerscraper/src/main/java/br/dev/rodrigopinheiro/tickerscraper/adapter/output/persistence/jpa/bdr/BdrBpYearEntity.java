package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bdr_bp_year")
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

    @Column(name = "ano")
    private Integer ano;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "ativos_totais", precision = 19, scale = 2)),
            @AttributeOverride(name = "quality", column = @Column(name = "ativos_totais_quality", length = 50)),
            @AttributeOverride(name = "raw", column = @Column(name = "ativos_totais_raw", columnDefinition = "TEXT"))
    })
    private AuditedBigDecimalEmbeddable ativosTotais;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "passivos_totais", precision = 19, scale = 2)),
            @AttributeOverride(name = "quality", column = @Column(name = "passivos_totais_quality", length = 50)),
            @AttributeOverride(name = "raw", column = @Column(name = "passivos_totais_raw", columnDefinition = "TEXT"))
    })
    private AuditedBigDecimalEmbeddable passivosTotais;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "divida_longo_prazo", precision = 19, scale = 2)),
            @AttributeOverride(name = "quality", column = @Column(name = "divida_longo_prazo_quality", length = 50)),
            @AttributeOverride(name = "raw", column = @Column(name = "divida_longo_prazo_raw", columnDefinition = "TEXT"))
    })
    private AuditedBigDecimalEmbeddable dividaLongoPrazo;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "pl", precision = 19, scale = 2)),
            @AttributeOverride(name = "quality", column = @Column(name = "pl_quality", length = 50)),
            @AttributeOverride(name = "raw", column = @Column(name = "pl_raw", columnDefinition = "TEXT"))
    })
    private AuditedBigDecimalEmbeddable pl;
}
