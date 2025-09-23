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

    @Column(name = "nome_indicador")
    private String nomeIndicador;

    @Column(name = "ano")
    private Integer ano;

    @Column(name = "valor", precision = 19, scale = 6)
    private BigDecimal valor;
}
