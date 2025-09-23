package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    @Column(name = "ativos_totais", precision = 19, scale = 2)
    private BigDecimal ativosTotais;

    @Column(name = "passivos_totais", precision = 19, scale = 2)
    private BigDecimal passivosTotais;

    @Column(name = "patrimonio_liquido", precision = 19, scale = 2)
    private BigDecimal patrimonioLiquido;

    @Column(name = "caixa_disponibilidades", precision = 19, scale = 2)
    private BigDecimal caixaEDisponibilidades;

    @Column(name = "divida_bruta", precision = 19, scale = 2)
    private BigDecimal dividaBruta;
}
