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

    @Column(name = "ultimo_preco", precision = 19, scale = 6)
    private BigDecimal ultimoPreco;

    @Column(name = "variacao_dia", precision = 19, scale = 6)
    private BigDecimal variacaoPercentualDia;

    @Column(name = "variacao_mes", precision = 19, scale = 6)
    private BigDecimal variacaoPercentualMes;

    @Column(name = "variacao_ano", precision = 19, scale = 6)
    private BigDecimal variacaoPercentualAno;

    @Column(name = "dividend_yield", precision = 19, scale = 6)
    private BigDecimal dividendYield;

    @Column(name = "preco_lucro", precision = 19, scale = 6)
    private BigDecimal precoLucro;

    @Column(name = "preco_valor_patrimonial", precision = 19, scale = 6)
    private BigDecimal precoValorPatrimonial;

    @Column(name = "valor_mercado", precision = 19, scale = 2)
    private BigDecimal valorMercado;

    @Column(name = "volume_medio", precision = 19, scale = 2)
    private BigDecimal volumeMedio;
}
