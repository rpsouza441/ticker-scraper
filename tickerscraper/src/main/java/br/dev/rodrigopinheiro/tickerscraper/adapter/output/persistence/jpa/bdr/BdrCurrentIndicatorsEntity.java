package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bdr_current_indicators")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdrCurrentIndicatorsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bdr_id", unique = true)
    private BdrEntity bdr;

    @Column(name = "pl", precision = 19, scale = 6)
    private BigDecimal pl;

    @Column(name = "pvp", precision = 19, scale = 6)
    private BigDecimal pvp;

    @Column(name = "psr", precision = 19, scale = 6)
    private BigDecimal psr;

    @Column(name = "p_ebit", precision = 19, scale = 6)
    private BigDecimal pEbit;

    @Column(name = "p_ebitda", precision = 19, scale = 6)
    private BigDecimal pEbitda;

    @Column(name = "p_ativo", precision = 19, scale = 6)
    private BigDecimal pAtivo;

    @Column(name = "roe", precision = 19, scale = 6)
    private BigDecimal roe;

    @Column(name = "roic", precision = 19, scale = 6)
    private BigDecimal roic;

    @Column(name = "roa", precision = 19, scale = 6)
    private BigDecimal roa;

    @Embedded
    private BdrCurrentMarginsEmbeddable margens;

    @Column(name = "vpa", precision = 19, scale = 6)
    private BigDecimal vpa;

    @Column(name = "lpa", precision = 19, scale = 6)
    private BigDecimal lpa;

    @Column(name = "patrimonio_por_ativos", precision = 19, scale = 6)
    private BigDecimal patrimonioPorAtivos;
}
