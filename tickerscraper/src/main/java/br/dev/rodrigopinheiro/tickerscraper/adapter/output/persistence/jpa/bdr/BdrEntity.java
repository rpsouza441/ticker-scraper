package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bdr")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdrEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticker", nullable = false, unique = true)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ativo", length = 20, nullable = false)
    @Builder.Default
    private TipoAtivo tipoAtivo = TipoAtivo.BDR;

    @Column(name = "nome_empresa")
    private String nomeEmpresa;

    @Column(name = "nome_acao_original")
    private String nomeAcaoOriginal;

    @Column(name = "codigo_negociacao")
    private String codigoNegociacao;

    @Column(name = "mercado")
    private String mercado;

    @Column(name = "pais_negociacao")
    private String paisDeNegociacao;

    @Column(name = "moeda_referencia")
    private String moedaDeReferencia;

    @Column(name = "preco_atual", precision = 19, scale = 6)
    private BigDecimal precoAtual;

    @Column(name = "variacao_dia", precision = 19, scale = 6)
    private BigDecimal variacaoDia;

    @Column(name = "variacao_mes", precision = 19, scale = 6)
    private BigDecimal variacaoMes;

    @Column(name = "variacao_ano", precision = 19, scale = 6)
    private BigDecimal variacaoAno;

    @Column(name = "dividend_yield", precision = 19, scale = 6)
    private BigDecimal dividendYield;

    @Column(name = "preco_alvo", precision = 19, scale = 6)
    private BigDecimal precoAlvo;

    @OneToMany(
            mappedBy = "bdr",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 50)
    @Builder.Default
    private List<BdrPriceSeriesEntity> priceSeries = new ArrayList<>();

    @OneToMany(
            mappedBy = "bdr",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 50)
    @Builder.Default
    private List<BdrDividendYearlyEntity> dividendYears = new ArrayList<>();

    @OneToMany(
            mappedBy = "bdr",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 50)
    @Builder.Default
    private List<BdrHistoricalIndicatorEntity> historicalIndicators = new ArrayList<>();

    @OneToMany(
            mappedBy = "bdr",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 50)
    @Builder.Default
    private List<BdrDreYearEntity> dreYears = new ArrayList<>();

    @OneToMany(
            mappedBy = "bdr",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 50)
    @Builder.Default
    private List<BdrBpYearEntity> bpYears = new ArrayList<>();

    @OneToMany(
            mappedBy = "bdr",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 50)
    @Builder.Default
    private List<BdrFcYearEntity> fcYears = new ArrayList<>();

    @OneToOne(
            mappedBy = "bdr",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private BdrCurrentIndicatorsEntity currentIndicators;

    @OneToOne(
            mappedBy = "bdr",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private BdrParidadeEntity paridade;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_json", columnDefinition = "JSONB")
    private String rawJson;

    @Column(name = "raw_json_hash", length = 128)
    private String rawJsonHash;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
