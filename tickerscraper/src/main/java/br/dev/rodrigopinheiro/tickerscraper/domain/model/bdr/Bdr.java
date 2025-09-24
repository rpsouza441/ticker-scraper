package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Bdr {

    private String ticker;
    private String investidorId;
    private TipoAtivo tipoAtivo = TipoAtivo.BDR;
    private String nomeBdr;
    private String setor;
    private String industria;
    private String priceCurrency;
    private String financialsCurrency;
    private BigDecimal cotacao;
    private BigDecimal variacao12;
    private BdrMarketCap marketCap;
    private ParidadeBdr paridade;
    private CurrentIndicators currentIndicators;
    private List<PricePoint> priceSeries;
    private List<DividendYear> dividendYears;
    private List<HistoricalIndicator> historicalIndicators;
    private List<DreYear> dreYears;
    private List<BpYear> bpYears;
    private List<FcYear> fcYears;
    private Instant updatedAt;
    private Map<String, Object> rawJson;
    private String rawJsonHash;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker == null ? null : ticker.trim().toUpperCase();
    }

    public String getInvestidorId() {
        return investidorId;
    }

    public void setInvestidorId(String investidorId) {
        this.investidorId = investidorId;
    }

    public TipoAtivo getTipoAtivo() {
        return tipoAtivo;
    }

    public void setTipoAtivo(TipoAtivo tipoAtivo) {
        this.tipoAtivo = tipoAtivo;
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
        this.priceCurrency = normalizeCurrency(priceCurrency);
    }

    public String getFinancialsCurrency() {
        return financialsCurrency;
    }

    public void setFinancialsCurrency(String financialsCurrency) {
        this.financialsCurrency = normalizeCurrency(financialsCurrency);
    }

    public BigDecimal getCotacao() {
        return cotacao;
    }

    public void setCotacao(BigDecimal cotacao) {
        this.cotacao = cotacao;
    }

    public BigDecimal getVariacao12() {
        return variacao12;
    }

    public void setVariacao12(BigDecimal variacao12) {
        this.variacao12 = variacao12;
    }

    public BdrMarketCap getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(BdrMarketCap marketCap) {
        this.marketCap = marketCap;
    }

    public ParidadeBdr getParidade() {
        return paridade;
    }

    public void setParidade(ParidadeBdr paridade) {
        this.paridade = paridade;
    }

    public CurrentIndicators getCurrentIndicators() {
        return currentIndicators;
    }

    public void setCurrentIndicators(CurrentIndicators currentIndicators) {
        this.currentIndicators = currentIndicators;
    }

    public List<PricePoint> getPriceSeries() {
        return priceSeries;
    }

    public void setPriceSeries(List<PricePoint> priceSeries) {
        this.priceSeries = priceSeries;
    }

    public List<DividendYear> getDividendYears() {
        return dividendYears;
    }

    public void setDividendYears(List<DividendYear> dividendYears) {
        this.dividendYears = dividendYears;
    }

    public List<HistoricalIndicator> getHistoricalIndicators() {
        return historicalIndicators;
    }

    public void setHistoricalIndicators(List<HistoricalIndicator> historicalIndicators) {
        this.historicalIndicators = historicalIndicators;
    }

    public List<DreYear> getDreYears() {
        return dreYears;
    }

    public void setDreYears(List<DreYear> dreYears) {
        this.dreYears = dreYears;
    }

    public List<BpYear> getBpYears() {
        return bpYears;
    }

    public void setBpYears(List<BpYear> bpYears) {
        this.bpYears = bpYears;
    }

    public List<FcYear> getFcYears() {
        return fcYears;
    }

    public void setFcYears(List<FcYear> fcYears) {
        this.fcYears = fcYears;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Object> getRawJson() {
        return rawJson;
    }

    public void setRawJson(Map<String, Object> rawJson) {
        this.rawJson = rawJson;
    }

    public String getRawJsonHash() {
        return rawJsonHash;
    }

    public void setRawJsonHash(String rawJsonHash) {
        this.rawJsonHash = rawJsonHash;
    }

    private String normalizeCurrency(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase();
    }
}
