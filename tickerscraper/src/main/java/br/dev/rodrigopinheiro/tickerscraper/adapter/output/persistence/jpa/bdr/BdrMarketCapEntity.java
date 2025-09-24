package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "bdr_market_cap")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdrMarketCapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bdr_id", unique = true)
    private BdrEntity bdr;

    @Column(name = "value", precision = 30, scale = 6)
    private BigDecimal value;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "quality", length = 32)
    private String quality;

    @Column(name = "raw", columnDefinition = "TEXT")
    private String raw;
}
