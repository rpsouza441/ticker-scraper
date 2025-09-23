package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Bdr {

    private String ticker;
    private String nomeEmpresa;
    private String nomeAcaoOriginal;
    private String codigoNegociacao;
    private String mercado;
    private String paisDeNegociacao;
    private String moedaDeReferencia;
    private BigDecimal precoAtual;
    private BigDecimal variacaoDia;
    private BigDecimal variacaoMes;
    private BigDecimal variacaoAno;
    private BigDecimal dividendYield;
    private BigDecimal precoAlvo;
    private TipoAtivo tipoAtivo = TipoAtivo.BDR;
    private ParidadeBdr paridade;
    private CurrentIndicators currentIndicators;
    private List<PricePoint> historicoDePrecos;
    private List<DividendYear> dividendosPorAno;
    private List<HistoricalIndicator> indicadoresHistoricos;
    private List<DreYear> dreAnual;
    private List<BpYear> bpAnual;
    private List<FcYear> fcAnual;
    private Instant updatedAt;
    private Map<String, Object> rawJson;
    private String rawJsonHash;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker == null ? null : ticker.trim().toUpperCase();
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public String getNomeAcaoOriginal() {
        return nomeAcaoOriginal;
    }

    public void setNomeAcaoOriginal(String nomeAcaoOriginal) {
        this.nomeAcaoOriginal = nomeAcaoOriginal;
    }

    public String getCodigoNegociacao() {
        return codigoNegociacao;
    }

    public void setCodigoNegociacao(String codigoNegociacao) {
        this.codigoNegociacao = codigoNegociacao;
    }

    public String getMercado() {
        return mercado;
    }

    public void setMercado(String mercado) {
        this.mercado = mercado;
    }

    public String getPaisDeNegociacao() {
        return paisDeNegociacao;
    }

    public void setPaisDeNegociacao(String paisDeNegociacao) {
        this.paisDeNegociacao = paisDeNegociacao;
    }

    public String getMoedaDeReferencia() {
        return moedaDeReferencia;
    }

    public void setMoedaDeReferencia(String moedaDeReferencia) {
        this.moedaDeReferencia = moedaDeReferencia;
    }

    public BigDecimal getPrecoAtual() {
        return precoAtual;
    }

    public void setPrecoAtual(BigDecimal precoAtual) {
        this.precoAtual = precoAtual;
    }

    public BigDecimal getVariacaoDia() {
        return variacaoDia;
    }

    public void setVariacaoDia(BigDecimal variacaoDia) {
        this.variacaoDia = variacaoDia;
    }

    public BigDecimal getVariacaoMes() {
        return variacaoMes;
    }

    public void setVariacaoMes(BigDecimal variacaoMes) {
        this.variacaoMes = variacaoMes;
    }

    public BigDecimal getVariacaoAno() {
        return variacaoAno;
    }

    public void setVariacaoAno(BigDecimal variacaoAno) {
        this.variacaoAno = variacaoAno;
    }

    public BigDecimal getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(BigDecimal dividendYield) {
        this.dividendYield = dividendYield;
    }

    public BigDecimal getPrecoAlvo() {
        return precoAlvo;
    }

    public void setPrecoAlvo(BigDecimal precoAlvo) {
        this.precoAlvo = precoAlvo;
    }

    public TipoAtivo getTipoAtivo() {
        return tipoAtivo;
    }

    public void setTipoAtivo(TipoAtivo tipoAtivo) {
        this.tipoAtivo = tipoAtivo;
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

    public List<PricePoint> getHistoricoDePrecos() {
        return historicoDePrecos;
    }

    public void setHistoricoDePrecos(List<PricePoint> historicoDePrecos) {
        this.historicoDePrecos = historicoDePrecos;
    }

    public List<DividendYear> getDividendosPorAno() {
        return dividendosPorAno;
    }

    public void setDividendosPorAno(List<DividendYear> dividendosPorAno) {
        this.dividendosPorAno = dividendosPorAno;
    }

    public List<HistoricalIndicator> getIndicadoresHistoricos() {
        return indicadoresHistoricos;
    }

    public void setIndicadoresHistoricos(List<HistoricalIndicator> indicadoresHistoricos) {
        this.indicadoresHistoricos = indicadoresHistoricos;
    }

    public List<DreYear> getDreAnual() {
        return dreAnual;
    }

    public void setDreAnual(List<DreYear> dreAnual) {
        this.dreAnual = dreAnual;
    }

    public List<BpYear> getBpAnual() {
        return bpAnual;
    }

    public void setBpAnual(List<BpYear> bpAnual) {
        this.bpAnual = bpAnual;
    }

    public List<FcYear> getFcAnual() {
        return fcAnual;
    }

    public void setFcAnual(List<FcYear> fcAnual) {
        this.fcAnual = fcAnual;
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
}
