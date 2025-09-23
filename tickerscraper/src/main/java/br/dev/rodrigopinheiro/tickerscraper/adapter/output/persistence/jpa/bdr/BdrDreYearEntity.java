package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bdr_dre_year")
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

    @Column(name = "ano")
    private Integer ano;

    @Column(name = "receita_liquida", precision = 19, scale = 2)
    private BigDecimal receitaLiquida;

    @Column(name = "lucro_liquido", precision = 19, scale = 2)
    private BigDecimal lucroLiquido;

    @Column(name = "ebitda", precision = 19, scale = 2)
    private BigDecimal ebitda;

    @Column(name = "ebit", precision = 19, scale = 2)
    private BigDecimal ebit;

    @Column(name = "margem_liquida", precision = 19, scale = 6)
    private BigDecimal margemLiquida;
}
