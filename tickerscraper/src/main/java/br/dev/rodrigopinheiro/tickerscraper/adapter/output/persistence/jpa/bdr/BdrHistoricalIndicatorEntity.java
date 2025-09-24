package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bdr_historical_indicator")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdrHistoricalIndicatorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bdr_id")
    private BdrEntity bdr;

    @Column(name = "year")
    private Integer year;

    @Column(name = "pl", precision = 18, scale = 6)
    private BigDecimal pl;

    @Column(name = "pvp", precision = 18, scale = 6)
    private BigDecimal pvp;

    @Column(name = "psr", precision = 18, scale = 6)
    private BigDecimal psr;

    @Column(name = "p_ebit", precision = 18, scale = 6)
    private BigDecimal pEbit;

    @Column(name = "p_ebitda", precision = 18, scale = 6)
    private BigDecimal pEbitda;

    @Column(name = "p_ativo", precision = 18, scale = 6)
    private BigDecimal pAtivo;

    @Column(name = "roe", precision = 18, scale = 6)
    private BigDecimal roe;

    @Column(name = "roic", precision = 18, scale = 6)
    private BigDecimal roic;

    @Column(name = "roa", precision = 18, scale = 6)
    private BigDecimal roa;

    @Column(name = "margem_bruta", precision = 18, scale = 6)
    private BigDecimal margemBruta;

    @Column(name = "margem_operacional", precision = 18, scale = 6)
    private BigDecimal margemOperacional;

    @Column(name = "margem_liquida", precision = 18, scale = 6)
    private BigDecimal margemLiquida;

    @Column(name = "vpa", precision = 18, scale = 6)
    private BigDecimal vpa;

    @Column(name = "lpa", precision = 18, scale = 6)
    private BigDecimal lpa;

    @Column(name = "patrimonio_por_ativos", precision = 18, scale = 6)
    private BigDecimal patrimonioPorAtivos;
}
