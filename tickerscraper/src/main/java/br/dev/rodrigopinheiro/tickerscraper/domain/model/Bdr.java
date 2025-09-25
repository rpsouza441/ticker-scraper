package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.ParidadeMethod;

import java.math.BigDecimal;
import java.time.Instant;

public class Bdr {
    // Identificação e auditoria
    private Integer investidorId;              // id interno do Investidor10 (quando conhecido)
    private Instant updatedAt;                 // instante UTC do snapshot

    // Identidade do papel
    private String ticker;                     // ex.: "AAPL34"
    private String nomeBdr;                    // ex.: "Apple"
    private String setor;
    private String industria;

    // Moedas/cotação
    private String priceCurrency;              // "BRL"
    private String financialsCurrency;         // "USD"
    private BigDecimal cotacao;                // BRL
    private Double variacao12;                 // % decimal (12.34 = 12,34%)

    // Market cap
    private BigDecimal marketCapValue;
    private String marketCapCurrency;          // "USD" | "BRL"

    // Paridade
    private BigDecimal paridadeRatio;          // BDRs por 1 ação (ex.: 20.0)
    private ParidadeMethod paridadeMethod;     // HTML | MANUAL | OUTRO
    private Instant paridadeLastVerifiedAt;

    // Indicadores (current)
    private BigDecimal pl;
    private BigDecimal pvp;
    private BigDecimal psr;
    private BigDecimal pEbit;
    private BigDecimal pEbitda;
    private BigDecimal pAtivo;
    private BigDecimal roe;                    // % como decimal ex.: 18.43
    private BigDecimal roic;
    private BigDecimal roa;
    private BigDecimal margemBruta;
    private BigDecimal margemOperacional;
    private BigDecimal margemLiquida;
    private BigDecimal vpa;
    private BigDecimal lpa;

    // DRE (último ano)
    private Integer dreYear;
    private BigDecimal receitaTotalUsd;
    private BigDecimal lucroBrutoUsd;
    private BigDecimal ebitdaUsd;
    private BigDecimal ebitUsd;
    private BigDecimal lucroLiquidoUsd;

    // BP (último ano)
    private Integer bpYear;
    private BigDecimal ativosTotaisUsd;
    private BigDecimal passivosTotaisUsd;
    private BigDecimal dividaLpUsd;
    private BigDecimal plUsd;

    // FC (último ano)
    private Integer fcYear;
    private BigDecimal fcoUsd;
    private BigDecimal fciUsd;
    private BigDecimal fcfUsd;

    // Dividendos (último ano)
    private Dividendo dividendo;

    public Bdr() {
    }

    public Bdr(Integer investidorId, Instant updatedAt, String ticker, String nomeBdr, String setor, String industria, String priceCurrency, String financialsCurrency, BigDecimal cotacao, Double variacao12, BigDecimal marketCapValue, String marketCapCurrency, BigDecimal paridadeRatio, ParidadeMethod paridadeMethod, Instant paridadeLastVerifiedAt, BigDecimal pl, BigDecimal pvp, BigDecimal psr, BigDecimal pEbit, BigDecimal pEbitda, BigDecimal pAtivo, BigDecimal roe, BigDecimal roic, BigDecimal roa, BigDecimal margemBruta, BigDecimal margemOperacional, BigDecimal margemLiquida, BigDecimal vpa, BigDecimal lpa, Integer dreYear, BigDecimal receitaTotalUsd, BigDecimal lucroBrutoUsd, BigDecimal ebitdaUsd, BigDecimal ebitUsd, BigDecimal lucroLiquidoUsd, Integer bpYear, BigDecimal ativosTotaisUsd, BigDecimal passivosTotaisUsd, BigDecimal dividaLpUsd, BigDecimal plUsd, Integer fcYear, BigDecimal fcoUsd, BigDecimal fciUsd, BigDecimal fcfUsd, Dividendo dividendo) {
        this.investidorId = investidorId;
        this.updatedAt = updatedAt;
        this.ticker = ticker;
        this.nomeBdr = nomeBdr;
        this.setor = setor;
        this.industria = industria;
        this.priceCurrency = priceCurrency;
        this.financialsCurrency = financialsCurrency;
        this.cotacao = cotacao;
        this.variacao12 = variacao12;
        this.marketCapValue = marketCapValue;
        this.marketCapCurrency = marketCapCurrency;
        this.paridadeRatio = paridadeRatio;
        this.paridadeMethod = paridadeMethod;
        this.paridadeLastVerifiedAt = paridadeLastVerifiedAt;
        this.pl = pl;
        this.pvp = pvp;
        this.psr = psr;
        this.pEbit = pEbit;
        this.pEbitda = pEbitda;
        this.pAtivo = pAtivo;
        this.roe = roe;
        this.roic = roic;
        this.roa = roa;
        this.margemBruta = margemBruta;
        this.margemOperacional = margemOperacional;
        this.margemLiquida = margemLiquida;
        this.vpa = vpa;
        this.lpa = lpa;
        this.dreYear = dreYear;
        this.receitaTotalUsd = receitaTotalUsd;
        this.lucroBrutoUsd = lucroBrutoUsd;
        this.ebitdaUsd = ebitdaUsd;
        this.ebitUsd = ebitUsd;
        this.lucroLiquidoUsd = lucroLiquidoUsd;
        this.bpYear = bpYear;
        this.ativosTotaisUsd = ativosTotaisUsd;
        this.passivosTotaisUsd = passivosTotaisUsd;
        this.dividaLpUsd = dividaLpUsd;
        this.plUsd = plUsd;
        this.fcYear = fcYear;
        this.fcoUsd = fcoUsd;
        this.fciUsd = fciUsd;
        this.fcfUsd = fcfUsd;
        this.dividendo = dividendo;
    }

    public Integer getInvestidorId() {
        return investidorId;
    }

    public void setInvestidorId(Integer investidorId) {
        this.investidorId = investidorId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getNomeBdr() {
        return nomeBdr;
    }

    public void setNomeBdr(String nomeBdr) {
        this.nomeBdr = nomeBdr;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getIndustria() {
        return industria;
    }

    public void setIndustria(String industria) {
        this.industria = industria;
    }

    public String getPriceCurrency() {
        return priceCurrency;
    }

    public void setPriceCurrency(String priceCurrency) {
        this.priceCurrency = priceCurrency;
    }

    public String getFinancialsCurrency() {
        return financialsCurrency;
    }

    public void setFinancialsCurrency(String financialsCurrency) {
        this.financialsCurrency = financialsCurrency;
    }

    public BigDecimal getCotacao() {
        return cotacao;
    }

    public void setCotacao(BigDecimal cotacao) {
        this.cotacao = cotacao;
    }

    public Double getVariacao12() {
        return variacao12;
    }

    public void setVariacao12(Double variacao12) {
        this.variacao12 = variacao12;
    }

    public BigDecimal getMarketCapValue() {
        return marketCapValue;
    }

    public void setMarketCapValue(BigDecimal marketCapValue) {
        this.marketCapValue = marketCapValue;
    }

    public String getMarketCapCurrency() {
        return marketCapCurrency;
    }

    public void setMarketCapCurrency(String marketCapCurrency) {
        this.marketCapCurrency = marketCapCurrency;
    }

    public BigDecimal getParidadeRatio() {
        return paridadeRatio;
    }

    public void setParidadeRatio(BigDecimal paridadeRatio) {
        this.paridadeRatio = paridadeRatio;
    }

    public ParidadeMethod getParidadeMethod() {
        return paridadeMethod;
    }

    public void setParidadeMethod(ParidadeMethod paridadeMethod) {
        this.paridadeMethod = paridadeMethod;
    }

    public Instant getParidadeLastVerifiedAt() {
        return paridadeLastVerifiedAt;
    }

    public void setParidadeLastVerifiedAt(Instant paridadeLastVerifiedAt) {
        this.paridadeLastVerifiedAt = paridadeLastVerifiedAt;
    }

    public BigDecimal getPl() {
        return pl;
    }

    public void setPl(BigDecimal pl) {
        this.pl = pl;
    }

    public BigDecimal getPvp() {
        return pvp;
    }

    public void setPvp(BigDecimal pvp) {
        this.pvp = pvp;
    }

    public BigDecimal getPsr() {
        return psr;
    }

    public void setPsr(BigDecimal psr) {
        this.psr = psr;
    }

    public BigDecimal getpEbit() {
        return pEbit;
    }

    public void setpEbit(BigDecimal pEbit) {
        this.pEbit = pEbit;
    }

    public BigDecimal getpEbitda() {
        return pEbitda;
    }

    public void setpEbitda(BigDecimal pEbitda) {
        this.pEbitda = pEbitda;
    }

    public BigDecimal getpAtivo() {
        return pAtivo;
    }

    public void setpAtivo(BigDecimal pAtivo) {
        this.pAtivo = pAtivo;
    }

    public BigDecimal getRoe() {
        return roe;
    }

    public void setRoe(BigDecimal roe) {
        this.roe = roe;
    }

    public BigDecimal getRoic() {
        return roic;
    }

    public void setRoic(BigDecimal roic) {
        this.roic = roic;
    }

    public BigDecimal getRoa() {
        return roa;
    }

    public void setRoa(BigDecimal roa) {
        this.roa = roa;
    }

    public BigDecimal getMargemBruta() {
        return margemBruta;
    }

    public void setMargemBruta(BigDecimal margemBruta) {
        this.margemBruta = margemBruta;
    }

    public BigDecimal getMargemOperacional() {
        return margemOperacional;
    }

    public void setMargemOperacional(BigDecimal margemOperacional) {
        this.margemOperacional = margemOperacional;
    }

    public BigDecimal getMargemLiquida() {
        return margemLiquida;
    }

    public void setMargemLiquida(BigDecimal margemLiquida) {
        this.margemLiquida = margemLiquida;
    }

    public BigDecimal getVpa() {
        return vpa;
    }

    public void setVpa(BigDecimal vpa) {
        this.vpa = vpa;
    }

    public BigDecimal getLpa() {
        return lpa;
    }

    public void setLpa(BigDecimal lpa) {
        this.lpa = lpa;
    }

    public Integer getDreYear() {
        return dreYear;
    }

    public void setDreYear(Integer dreYear) {
        this.dreYear = dreYear;
    }

    public BigDecimal getReceitaTotalUsd() {
        return receitaTotalUsd;
    }

    public void setReceitaTotalUsd(BigDecimal receitaTotalUsd) {
        this.receitaTotalUsd = receitaTotalUsd;
    }

    public BigDecimal getLucroBrutoUsd() {
        return lucroBrutoUsd;
    }

    public void setLucroBrutoUsd(BigDecimal lucroBrutoUsd) {
        this.lucroBrutoUsd = lucroBrutoUsd;
    }

    public BigDecimal getEbitdaUsd() {
        return ebitdaUsd;
    }

    public void setEbitdaUsd(BigDecimal ebitdaUsd) {
        this.ebitdaUsd = ebitdaUsd;
    }

    public BigDecimal getEbitUsd() {
        return ebitUsd;
    }

    public void setEbitUsd(BigDecimal ebitUsd) {
        this.ebitUsd = ebitUsd;
    }

    public BigDecimal getLucroLiquidoUsd() {
        return lucroLiquidoUsd;
    }

    public void setLucroLiquidoUsd(BigDecimal lucroLiquidoUsd) {
        this.lucroLiquidoUsd = lucroLiquidoUsd;
    }

    public Integer getBpYear() {
        return bpYear;
    }

    public void setBpYear(Integer bpYear) {
        this.bpYear = bpYear;
    }

    public BigDecimal getAtivosTotaisUsd() {
        return ativosTotaisUsd;
    }

    public void setAtivosTotaisUsd(BigDecimal ativosTotaisUsd) {
        this.ativosTotaisUsd = ativosTotaisUsd;
    }

    public BigDecimal getPassivosTotaisUsd() {
        return passivosTotaisUsd;
    }

    public void setPassivosTotaisUsd(BigDecimal passivosTotaisUsd) {
        this.passivosTotaisUsd = passivosTotaisUsd;
    }

    public BigDecimal getDividaLpUsd() {
        return dividaLpUsd;
    }

    public void setDividaLpUsd(BigDecimal dividaLpUsd) {
        this.dividaLpUsd = dividaLpUsd;
    }

    public BigDecimal getPlUsd() {
        return plUsd;
    }

    public void setPlUsd(BigDecimal plUsd) {
        this.plUsd = plUsd;
    }

    public Integer getFcYear() {
        return fcYear;
    }

    public void setFcYear(Integer fcYear) {
        this.fcYear = fcYear;
    }

    public BigDecimal getFcoUsd() {
        return fcoUsd;
    }

    public void setFcoUsd(BigDecimal fcoUsd) {
        this.fcoUsd = fcoUsd;
    }

    public BigDecimal getFciUsd() {
        return fciUsd;
    }

    public void setFciUsd(BigDecimal fciUsd) {
        this.fciUsd = fciUsd;
    }

    public BigDecimal getFcfUsd() {
        return fcfUsd;
    }

    public void setFcfUsd(BigDecimal fcfUsd) {
        this.fcfUsd = fcfUsd;
    }

    public Dividendo getDividendo() {
        return dividendo;
    }

    public void setDividendo(Dividendo dividendo) {
        this.dividendo = dividendo;
    }
}
