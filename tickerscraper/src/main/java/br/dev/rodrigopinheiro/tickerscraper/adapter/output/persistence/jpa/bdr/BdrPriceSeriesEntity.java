package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bdr_price_series")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdrPriceSeriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bdr_id")
    private BdrEntity bdr;

    @Column(name = "dt", nullable = false)
    private OffsetDateTime dt;

    @Column(name = "close", precision = 19, scale = 6, nullable = false)
    private BigDecimal close;
}
