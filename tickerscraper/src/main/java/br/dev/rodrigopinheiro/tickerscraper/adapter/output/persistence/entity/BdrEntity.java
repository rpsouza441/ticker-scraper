package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.Dividendo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bdr", indexes = {
        @Index(name = "ix_bdr_ticker", columnList = "ticker"),
        @Index(name = "ix_bdr_updated_at", columnList = "updated_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Access(AccessType.FIELD)
public class BdrEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ================= Identificação e auditoria =================

    @Column(name = "investidor_id")
    private Integer investidorId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ================= Identidade do papel =================

    @Column(name = "ticker", length = 20, nullable = false)
    private String ticker;

    @Column(name = "nome_bdr", length = 200)
    private String nomeBdr;

    @Column(name = "setor", length = 120)
    private String setor;

    @Column(name = "industria", length = 160)
    private String industria;

    // ================= Moedas / cotação =================

    @Column(name = "price_currency", length = 3, nullable = false)
    private String priceCurrency;              // "BRL"

    @Column(name = "financials_currency", length = 3, nullable = false)
    private String financialsCurrency;         // "USD"

    @Column(name = "cotacao", precision = 19, scale = 6)
    private BigDecimal cotacao;                // BRL

    @Column(name = "variacao_12")
    private Double variacao12;                 // % decimal (12.34 = 12,34%)

    // ================= Market cap =================

    @Column(name = "market_cap_value", precision = 24, scale = 2)
    private BigDecimal marketCapValue;

    @Column(name = "market_cap_currency", length = 3)
    private String marketCapCurrency;          // "USD" | "BRL"

    // ================= Paridade =================

    @Column(name = "paridade_ratio", precision = 19, scale = 6)
    private BigDecimal paridadeRatio;          // BDRs por 1 ação (ex.: 20.0)

    @Column(name = "paridade_last_verified_at")
    private Instant paridadeLastVerifiedAt;

    // ================= Indicadores (current) =================

    @Column(name = "pl", precision = 19, scale = 6)
    private BigDecimal pl;

    @Column(name = "pvp", precision = 19, scale = 6)
    private BigDecimal pvp;

    @Column(name = "psr", precision = 19, scale = 6)
    private BigDecimal psr;

    @Column(name = "p_ebit", precision = 19, scale = 6)
    private BigDecimal pEbit;

    @Column(name = "p_ebitda", precision = 19, scale = 6)
    private BigDecimal pEbitda;

    @Column(name = "p_ativo", precision = 19, scale = 6)
    private BigDecimal pAtivo;

    @Column(name = "roe", precision = 19, scale = 6)
    private BigDecimal roe;                    // % decimal ex.: 18.43

    @Column(name = "roic", precision = 19, scale = 6)
    private BigDecimal roic;

    @Column(name = "roa", precision = 19, scale = 6)
    private BigDecimal roa;

    @Column(name = "margem_bruta", precision = 19, scale = 6)
    private BigDecimal margemBruta;

    @Column(name = "margem_operacional", precision = 19, scale = 6)
    private BigDecimal margemOperacional;

    @Column(name = "margem_liquida", precision = 19, scale = 6)
    private BigDecimal margemLiquida;

    @Column(name = "vpa", precision = 19, scale = 6)
    private BigDecimal vpa;

    @Column(name = "lpa", precision = 19, scale = 6)
    private BigDecimal lpa;

    // ================= DRE (último ano) =================

    @Column(name = "dre_year")
    private Integer dreYear;

    @Column(name = "receita_total_usd", precision = 24, scale = 2)
    private BigDecimal receitaTotalUsd;

    @Column(name = "lucro_bruto_usd", precision = 24, scale = 2)
    private BigDecimal lucroBrutoUsd;

    @Column(name = "ebitda_usd", precision = 24, scale = 2)
    private BigDecimal ebitdaUsd;

    @Column(name = "ebit_usd", precision = 24, scale = 2)
    private BigDecimal ebitUsd;

    @Column(name = "lucro_liquido_usd", precision = 24, scale = 2)
    private BigDecimal lucroLiquidoUsd;

    // ================= Balanço Patrimonial (último ano) =================

    @Column(name = "bp_year")
    private Integer bpYear;

    @Column(name = "ativos_totais_usd", precision = 24, scale = 2)
    private BigDecimal ativosTotaisUsd;

    @Column(name = "passivos_totais_usd", precision = 24, scale = 2)
    private BigDecimal passivosTotaisUsd;

    @Column(name = "divida_lp_usd", precision = 24, scale = 2)
    private BigDecimal dividaLpUsd;

    @Column(name = "pl_usd", precision = 24, scale = 2)
    private BigDecimal plUsd;

    // ================= Fluxo de Caixa (último ano) =================

    @Column(name = "fc_year")
    private Integer fcYear;

    @Column(name = "fco_usd", precision = 24, scale = 2)
    private BigDecimal fcoUsd;

    @Column(name = "fci_usd", precision = 24, scale = 2)
    private BigDecimal fciUsd;

    @Column(name = "fcf_usd", precision = 24, scale = 2)
    private BigDecimal fcfUsd;

    // ================= Dividendos (último ano) =================
    // Persistido como JSON para manter o campo do domínio sem quebrar JPA.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dividendo_id", foreignKey = @ForeignKey(name = "fk_bdr_dividendo"))
    private DividendoEntity dividendo;

}
