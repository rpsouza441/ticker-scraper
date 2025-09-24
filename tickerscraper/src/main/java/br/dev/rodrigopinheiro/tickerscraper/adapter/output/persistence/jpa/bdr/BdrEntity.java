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

    @Column(name = "investidor_id", length = 100)
    private String investidorId;

    @Column(name = "nome_bdr")
    private String nomeBdr;

    @Column(name = "setor")
    private String setor;

    @Column(name = "industria")
    private String industria;

    @Column(name = "price_currency", length = 3)
    private String priceCurrency;

    @Column(name = "financials_currency", length = 3)
    private String financialsCurrency;

    @Column(name = "cotacao", precision = 19, scale = 6)
    private BigDecimal cotacao;

    @Column(name = "variacao_12", precision = 19, scale = 6)
    private BigDecimal variacao12;

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

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
