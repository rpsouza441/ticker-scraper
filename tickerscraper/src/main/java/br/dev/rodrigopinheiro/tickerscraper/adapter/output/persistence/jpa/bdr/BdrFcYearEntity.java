package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bdr_fc_year")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdrFcYearEntity {

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
            @AttributeOverride(name = "valor", column = @Column(name = "fco_val", precision = 30, scale = 6)),
            @AttributeOverride(name = "quality", column = @Column(name = "fco_qual")),
            @AttributeOverride(name = "raw", column = @Column(name = "fco_raw"))
    })
    private QualityValueEmbeddable fluxoCaixaOperacional;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valor", column = @Column(name = "fci_val", precision = 30, scale = 6)),
            @AttributeOverride(name = "quality", column = @Column(name = "fci_qual")),
            @AttributeOverride(name = "raw", column = @Column(name = "fci_raw"))
    })
    private QualityValueEmbeddable fluxoCaixaInvestimento;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valor", column = @Column(name = "fcf_val", precision = 30, scale = 6)),
            @AttributeOverride(name = "quality", column = @Column(name = "fcf_qual")),
            @AttributeOverride(name = "raw", column = @Column(name = "fcf_raw"))
    })
    private QualityValueEmbeddable fluxoCaixaFinanciamento;
}
