package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bdr_dividend_year")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdrDividendYearlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bdr_id")
    private BdrEntity bdr;

    @Column(name = "year")
    private Integer year;

    @Column(name = "valor", precision = 19, scale = 6)
    private BigDecimal valor;

    @Column(name = "dividend_yield", precision = 19, scale = 6)
    private BigDecimal dividendYield;

    @Builder.Default
    @Column(name = "currency", nullable = false)
    private String currency = "USD";
}
