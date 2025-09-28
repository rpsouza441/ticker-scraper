package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class Bdr extends AtivoFinanceiro {

    // Identidade do papel
    private String setor;
    private String industria;

    // Moedas/cotação
    private String priceCurrency;              // "BRL"
    private String financialsCurrency;         // "USD"

    // Market cap
    private BigDecimal marketCapValue;
    private String marketCapCurrency;          // "USD" | "BRL"

    // Paridade
    private BigDecimal paridadeRatio;          // BDRs por 1 ação (ex.: 20.0)
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


    public Bdr() {
        super();
    }

    public Bdr(Integer investidorId, String ticker, String nome, BigDecimal precoAtual, BigDecimal variacao12M, BigDecimal dividendYield, Instant dataAtualizacao, TipoAtivo tipoAtivo, List<Dividendo> dividendos, String setor, String industria, String priceCurrency, String financialsCurrency, BigDecimal marketCapValue, String marketCapCurrency, BigDecimal paridadeRatio, Instant paridadeLastVerifiedAt, BigDecimal pl, BigDecimal pvp, BigDecimal psr, BigDecimal pEbit, BigDecimal pEbitda, BigDecimal pAtivo, BigDecimal roe, BigDecimal roic, BigDecimal roa, BigDecimal margemBruta, BigDecimal margemOperacional, BigDecimal margemLiquida, BigDecimal vpa, BigDecimal lpa, Integer dreYear, BigDecimal receitaTotalUsd, BigDecimal lucroBrutoUsd, BigDecimal ebitdaUsd, BigDecimal ebitUsd, BigDecimal lucroLiquidoUsd, Integer bpYear, BigDecimal ativosTotaisUsd, BigDecimal passivosTotaisUsd, BigDecimal dividaLpUsd, BigDecimal plUsd, Integer fcYear, BigDecimal fcoUsd, BigDecimal fciUsd, BigDecimal fcfUsd) {
        super(investidorId, ticker, nome, precoAtual, variacao12M, dividendYield, dataAtualizacao, tipoAtivo, dividendos);
        this.setor = setor;
        this.industria = industria;
        this.priceCurrency = priceCurrency;
        this.financialsCurrency = financialsCurrency;
        this.marketCapValue = marketCapValue;
        this.marketCapCurrency = marketCapCurrency;
        this.paridadeRatio = paridadeRatio;
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
}
