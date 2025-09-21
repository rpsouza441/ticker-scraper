package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fundo_imobiliario")
public class FundoImobiliarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @Column(name = "ticker", nullable = false)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ativo", length = 20, nullable = false)
    @Builder.Default
    private TipoAtivo tipoAtivo = TipoAtivo.FII;

    @Column(name = "internal_id", nullable = false)
    private Long internalId;

    @Column(name = "nome_empresa")
    private String nomeEmpresa;

    @Column(name = "razao_social")
    private String razaoSocial;

    @Column(name = "cnpj")
    private String cnpj;

    @Column(name = "publico_alvo")
    private String publicoAlvo;

    @Column(name = "mandato")
    private String mandato;

    @Column(name = "segmento")
    private String segmento;

    @Column(name = "tipo_de_fundo")
    private String tipoDeFundo;

    @Column(name = "prazo_de_duracao")
    private String prazoDeDuracao;

    @Column(name = "tipo_de_gestao")
    private String tipoDeGestao;

    @Column(name = "taxa_de_administracao", precision = 5, scale = 2)
    private BigDecimal taxaDeAdministracao;

    @Column(name = "ultimo_rendimento", precision = 19, scale = 2)
    private BigDecimal ultimoRendimento;

    @Column(name = "cotacao", precision = 19, scale = 2)
    private BigDecimal cotacao;

    @Column(name = "variacao_12m", precision = 5, scale = 2)
    private BigDecimal variacao12M;

    @Column(name = "valor_de_mercado", precision = 19, scale = 2)
    private BigDecimal valorDeMercado;

    @Column(name = "pvp", precision = 19, scale = 6)
    private BigDecimal pvp;

    @Column(name = "dividend_yield", precision = 5, scale = 2)
    private BigDecimal dividendYield;

    @Column(name = "liquidez_diaria", precision = 19, scale = 2)
    private BigDecimal liquidezDiaria;

    @Column(name = "valor_patrimonial", precision = 19, scale = 2)
    private BigDecimal valorPatrimonial;

    @Column(name = "valor_patrimonial_por_cota", precision = 19, scale = 2)
    private BigDecimal valorPatrimonialPorCota;

    @Column(name = "vacancia", precision = 5, scale = 2)
    private BigDecimal vacancia;

    @Column(name = "numero_de_cotistas")
    private Long numeroDeCotistas;

    @Column(name = "cotas_emitidas")
    private Long cotasEmitidas;

    @OneToMany(
            mappedBy = "fundoImobiliario",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 50)
    @Builder.Default
    private List<FiiDividendoEntity> fiiDividendos = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_brutos_json", columnDefinition = "JSONB")
    private String dadosBrutosJson;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
}
