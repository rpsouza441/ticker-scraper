package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "acao")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticker", unique = true, nullable = false)
    private String ticker;

    @Column(name = "nome_empresa")
    private String nomeEmpresa;

    @Column(name = "setor")
    private String setor;

    @Column(name = "segmento")
    private String segmento;

    @Column(name = "segmento_listagem")
    private String segmentoListagem;

    @Column(name = "preco_atual")
    private BigDecimal precoAtual;

    @Column(name = "variacao_12m")
    private BigDecimal variacao12M;

    @Column(name = "valor_mercado")
    private BigDecimal valorMercado;

    @Column(name = "valor_firma")
    private BigDecimal valorFirma;

    @Column(name = "patrimonio_liquido")
    private BigDecimal patrimonioLiquido;

    @Column(name = "numero_total_papeis")
    private BigDecimal numeroTotalPapeis;

    @Column(name = "ativos")
    private BigDecimal ativos;

    @Column(name = "ativo_circulantes")
    private BigDecimal ativoCirculantes;

    @Column(name = "divida_bruta")
    private BigDecimal dividaBruta;

    @Column(name = "divida_liquida")
    private BigDecimal dividaLiquida;

    @Column(name = "disponibilidade")
    private String disponibilidade;

    @Column(name = "free_float")
    private BigDecimal freeFloat;

    @Column(name = "tag_along")
    private BigDecimal tagAlong;

    @Column(name = "liquidez_media_diaria")
    private BigDecimal liquidezMediaDiaria;

    @Column(name = "p_l")
    private BigDecimal pL;

    @Column(name = "psr")
    private BigDecimal psr;

    @Column(name = "p_vp")
    private BigDecimal pVp;

    @Column(name = "dividend_yeld")
    private BigDecimal dividendYeld;

    @Column(name = "payout")
    private BigDecimal payout;

    @Column(name = "margem_liquida")
    private BigDecimal margemLiquida;

    @Column(name = "margem_bruta")
    private BigDecimal margemBruta;

    @Column(name = "margem_ebit")
    private BigDecimal margemEbit;

    @Column(name = "margem_ebitda")
    private BigDecimal margemEbitda;

    @Column(name = "ev_ebitda")
    private BigDecimal evEbitda;

    @Column(name = "ev_ebit")
    private BigDecimal evEbit;

    @Column(name = "p_ebitda")
    private BigDecimal pEbitda;

    @Column(name = "p_ativo")
    private BigDecimal pAtivo;

    @Column(name = "p_capital_de_giro")
    private BigDecimal pCapitaldeGiro;

    @Column(name = "p_ativo_circulante_liquido")
    private BigDecimal pAtivoCirculanteLiquido;

    @Column(name = "vpa")
    private BigDecimal vpa;

    @Column(name = "lpa")
    private BigDecimal lpa;

    @Column(name = "giro_ativos")
    private BigDecimal giroAtivos;

    @Column(name = "roe")
    private BigDecimal roe;

    @Column(name = "roic")
    private BigDecimal roic;

    @Column(name = "roa")
    private BigDecimal roa;

    @Column(name = "divida_liquida_patrimonio")
    private BigDecimal dividaLiquidaPatrimonio;

    @Column(name = "divida_liquida_ebitda")
    private BigDecimal dividaLiquidaEbitda;

    @Column(name = "divida_liquida_ebit")
    private BigDecimal dividaLiquidaEbit;

    @Column(name = "divida_bruta_patrimonio")
    private BigDecimal dividaBrutaPatrimonio;

    @Column(name = "patrimonio_ativos")
    private BigDecimal patrimonioAtivos;

    @Column(name = "passivos_ativos")
    private BigDecimal passivosAtivos;

    @Column(name = "liquidez_corrente")
    private BigDecimal liquidezCorrente;

    @Column(name = "cagr_receitas_cinco_anos")
    private BigDecimal cagrReceitasCincoAnos;

    @Column(name = "cagr_lucros_cinco_anos")
    private BigDecimal cagrLucrosCincoAnos;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_brutos_json", columnDefinition = "JSONB")
    private String dadosBrutosJson;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        AcaoEntity that = (AcaoEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
