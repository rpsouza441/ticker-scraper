package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ativo_financeiro",
        indexes = {
                @Index(name = "ux_ativo_ticker", columnList = "ticker", unique = true),
                @Index(name = "ix_ativo_data_atualizacao", columnList = "data_atualizacao")
        })
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AtivoFinanceiroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticker", nullable = false, unique = true, length = 20)
    private String ticker;

    @Column(name = "nome", length = 200)
    private String nome;

    @Column(name = "investidor_id")
    private Integer investidorId;

    @Column(name = "preco_atual", precision = 19, scale = 6)
    private BigDecimal precoAtual;

    @Column(name = "variacao_12m", precision = 19, scale = 6)
    private BigDecimal variacao12M;

    @Column(name = "dividend_yield", precision = 19, scale = 6)
    private BigDecimal dividendYield;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private Instant dataAtualizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ativo", nullable = false, length = 50)
    private TipoAtivo tipoAtivo;

    @OneToMany(
            mappedBy = "ativo",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DividendoEntity> dividendos = new ArrayList<>();


}

