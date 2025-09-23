package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bdr_paridade")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdrParidadeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bdr_id", unique = true)
    private BdrEntity bdr;

    @Column(name = "fator_conversao", precision = 19, scale = 6)
    private BigDecimal fatorConversao;

    @Column(name = "ticker_original")
    private String tickerOriginal;

    @Column(name = "bolsa_origem")
    private String bolsaOrigem;

    @Column(name = "moeda_origem")
    private String moedaOrigem;
}
