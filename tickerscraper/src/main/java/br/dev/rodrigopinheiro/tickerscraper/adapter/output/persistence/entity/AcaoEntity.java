package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "acao")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticker")
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
    private String precoAtual;

    @Column(name = "variacao_12m")
    private double variacao12M;

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
    private BigDecimal disponibilidade;

    @Column(name = "free_float")
    private double freeFloat;

    @Column(name = "tag_along")
    private double tagAlong;

    @Column(name = "liquidez_media_diaria")
    private BigDecimal liquidezMediaDiaria;

    @Column(name = "p_l")
    private double pL;

    @Column(name = "psr")
    private double psr;

    @Column(name = "p_vp")
    private double pVp;

    @Column(name = "dividend_yeld")
    private double dividendYeld;

    @Column(name = "payout")
    private double payout;

    @Column(name = "margem_liquida")
    private double margemLiquida;

    @Column(name = "margem_bruta")
    private double margemBruta;

    @Column(name = "margem_ebit")
    private double margemEbit;

    @Column(name = "margem_ebitda")
    private double margemEbitda;

    @Column(name = "ev_ebitda")
    private double evEbitda;

    @Column(name = "ev_ebit")
    private double evEbit;

    @Column(name = "p_ebitda")
    private double pEbitda;

    @Column(name = "p_ativo")
    private double pAtivo;

    @Column(name = "p_capital_de_giro")
    private double pCapitaldeGiro;

    @Column(name = "p_ativo_circulante_liquido")
    private double pAtivoCirculanteLiquido;

    @Column(name = "vpa")
    private double vpa;

    @Column(name = "lpa")
    private double lpa;

    @Column(name = "giro_ativos")
    private double giroAtivos;

    @Column(name = "roe")
    private double roe;

    @Column(name = "roic")
    private double roic;

    @Column(name = "roa")
    private double roa;

    @Column(name = "divida_liquida_patrimonio")
    private double dividaLiquidaPatrimonio;

    @Column(name = "divida_liquida_ebitda")
    private double dividaLiquidaEbitda;

    @Column(name = "divida_liquida_ebit")
    private double dividaLiquidaEbit;

    @Column(name = "divida_bruta_patrimonio")
    private double dividaBrutaPatrimonio;

    @Column(name = "patrimonio_ativos")
    private double patrimonioAtivos;

    @Column(name = "passivos_ativos")
    private double passivosAtivos;

    @Column(name = "liquidez_corrente")
    private double liquidezCorrente;

    @Column(name = "cagr_receitas_cinco_anos")
    private double cagrReceitasCincoAnos;

    @Column(name = "cagr_lucros_cinco_anos")
    private double cagrLucrosCincoAnos;

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
