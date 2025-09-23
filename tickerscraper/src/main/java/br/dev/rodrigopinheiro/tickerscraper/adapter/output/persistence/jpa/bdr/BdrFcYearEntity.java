package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    @Column(name = "fluxo_operacional", precision = 19, scale = 2)
    private BigDecimal fluxoCaixaOperacional;

    @Column(name = "fluxo_investimento", precision = 19, scale = 2)
    private BigDecimal fluxoCaixaInvestimento;

    @Column(name = "fluxo_financiamento", precision = 19, scale = 2)
    private BigDecimal fluxoCaixaFinanciamento;

    @Column(name = "caixa_final", precision = 19, scale = 2)
    private BigDecimal caixaFinal;
}
