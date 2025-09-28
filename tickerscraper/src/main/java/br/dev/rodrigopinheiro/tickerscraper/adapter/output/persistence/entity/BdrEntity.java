package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.Dividendo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "bdr")
@DiscriminatorValue("BDR") // Define o valor que será salvo na coluna 'dtype' da tabela pai
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BdrEntity extends AtivoFinanceiroEntity {

    // Os campos de AtivoFinanceiroEntity (id, ticker, nome, etc.) são herdados automaticamente.
    // Não é preciso redeclará-los.

    // =========================================================
    // Campos específicos de BdrEntity
    // =========================================================

    @Column(name = "setor", length = 120)
    private String setor;

    @Column(name = "industria", length = 160)
    private String industria;

    @Column(name = "price_currency", nullable = false, length = 3)
    private String priceCurrency;

    @Column(name = "financials_currency", nullable = false, length = 3)
    private String financialsCurrency;

    @Column(name = "market_cap_value", precision = 24, scale = 2)
    private BigDecimal marketCapValue;

    @Column(name = "market_cap_currency", length = 3)
    private String marketCapCurrency;

    @Column(name = "paridade_ratio", precision = 19, scale = 6)
    private BigDecimal paridadeRatio;

    @Column(name = "paridade_last_verified_at")
    private Instant paridadeLastVerifiedAt;

    // --- Indicadores ---
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
    private BigDecimal roe;
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

    // --- DRE ---
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

    // --- BP ---
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

    // --- FC ---
    @Column(name = "fc_year")
    private Integer fcYear;
    @Column(name = "fco_usd", precision = 24, scale = 2)
    private BigDecimal fcoUsd;
    @Column(name = "fci_usd", precision = 24, scale = 2)
    private BigDecimal fciUsd;
    @Column(name = "fcf_usd", precision = 24, scale = 2)
    private BigDecimal fcfUsd;

}