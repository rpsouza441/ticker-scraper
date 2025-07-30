package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import java.math.BigDecimal;
import java.util.List;

public class FundoImobiliario {
    private String ticker;
    private String nomeEmpresa;



    private String razaoSocial;
    private String cnpj;
    private String publicoAlvo;
    private String mandato;
    private String segmento;
    private String tipoDeFundo;
    private String prazoDeDuracao;
    private String tipoDeGestao;
    private BigDecimal taxaDeAdministracao;
    private BigDecimal ultimoRendimento;

    private List<FiiDividendo> fiiDividendo;

    private BigDecimal cotacao;

    private BigDecimal valorDeMercado;
    private BigDecimal pvp;
    private BigDecimal dividendYield;
    private BigDecimal liquidezDiaria;
    private BigDecimal valorPatrimonial;
    private BigDecimal valorPatrimonialPorCota;
    private BigDecimal vacancia;
    private BigDecimal numeroDeCotistas;
    private BigDecimal cotasEmitidas;

    public FundoImobiliario() {
    }

    public FundoImobiliario(String ticker, String nomeEmpresa, String razaoSocial, String cnpj, String publicoAlvo, String mandato, String segmento, String tipoDeFundo, String prazoDeDuracao, String tipoDeGestao, BigDecimal taxaDeAdministracao, BigDecimal ultimoRendimento, List<FiiDividendo> fiiDividendo, BigDecimal cotacao, BigDecimal valorDeMercado, BigDecimal pvp, BigDecimal dividendYield, BigDecimal liquidezDiaria, BigDecimal valorPatrimonial, BigDecimal valorPatrimonialPorCota, BigDecimal vacancia, BigDecimal numeroDeCotistas, BigDecimal cotasEmitidas) {
        this.ticker = ticker;
        this.nomeEmpresa = nomeEmpresa;
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.publicoAlvo = publicoAlvo;
        this.mandato = mandato;
        this.segmento = segmento;
        this.tipoDeFundo = tipoDeFundo;
        this.prazoDeDuracao = prazoDeDuracao;
        this.tipoDeGestao = tipoDeGestao;
        this.taxaDeAdministracao = taxaDeAdministracao;
        this.ultimoRendimento = ultimoRendimento;
        this.fiiDividendo = fiiDividendo;
        this.cotacao = cotacao;
        this.valorDeMercado = valorDeMercado;
        this.pvp = pvp;
        this.dividendYield = dividendYield;
        this.liquidezDiaria = liquidezDiaria;
        this.valorPatrimonial = valorPatrimonial;
        this.valorPatrimonialPorCota = valorPatrimonialPorCota;
        this.vacancia = vacancia;
        this.numeroDeCotistas = numeroDeCotistas;
        this.cotasEmitidas = cotasEmitidas;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getPublicoAlvo() {
        return publicoAlvo;
    }

    public void setPublicoAlvo(String publicoAlvo) {
        this.publicoAlvo = publicoAlvo;
    }

    public String getMandato() {
        return mandato;
    }

    public void setMandato(String mandato) {
        this.mandato = mandato;
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

    public String getTipoDeFundo() {
        return tipoDeFundo;
    }

    public void setTipoDeFundo(String tipoDeFundo) {
        this.tipoDeFundo = tipoDeFundo;
    }

    public String getPrazoDeDuracao() {
        return prazoDeDuracao;
    }

    public void setPrazoDeDuracao(String prazoDeDuracao) {
        this.prazoDeDuracao = prazoDeDuracao;
    }

    public String getTipoDeGestao() {
        return tipoDeGestao;
    }

    public void setTipoDeGestao(String tipoDeGestao) {
        this.tipoDeGestao = tipoDeGestao;
    }

    public BigDecimal getTaxaDeAdministracao() {
        return taxaDeAdministracao;
    }

    public void setTaxaDeAdministracao(BigDecimal taxaDeAdministracao) {
        this.taxaDeAdministracao = taxaDeAdministracao;
    }

    public BigDecimal getUltimoRendimento() {
        return ultimoRendimento;
    }

    public void setUltimoRendimento(BigDecimal ultimoRendimento) {
        this.ultimoRendimento = ultimoRendimento;
    }

    public List<FiiDividendo> getFiiDividendo() {
        return fiiDividendo;
    }

    public void setFiiDividendo(List<FiiDividendo> fiiDividendo) {
        this.fiiDividendo = fiiDividendo;
    }

    public BigDecimal getCotacao() {
        return cotacao;
    }

    public void setCotacao(BigDecimal cotacao) {
        this.cotacao = cotacao;
    }

    public BigDecimal getValorDeMercado() {
        return valorDeMercado;
    }

    public void setValorDeMercado(BigDecimal valorDeMercado) {
        this.valorDeMercado = valorDeMercado;
    }

    public BigDecimal getPvp() {
        return pvp;
    }

    public void setPvp(BigDecimal pvp) {
        this.pvp = pvp;
    }

    public BigDecimal getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(BigDecimal dividendYield) {
        this.dividendYield = dividendYield;
    }

    public BigDecimal getLiquidezDiaria() {
        return liquidezDiaria;
    }

    public void setLiquidezDiaria(BigDecimal liquidezDiaria) {
        this.liquidezDiaria = liquidezDiaria;
    }

    public BigDecimal getValorPatrimonial() {
        return valorPatrimonial;
    }

    public void setValorPatrimonial(BigDecimal valorPatrimonial) {
        this.valorPatrimonial = valorPatrimonial;
    }

    public BigDecimal getValorPatrimonialPorCota() {
        return valorPatrimonialPorCota;
    }

    public void setValorPatrimonialPorCota(BigDecimal valorPatrimonialPorCota) {
        this.valorPatrimonialPorCota = valorPatrimonialPorCota;
    }

    public BigDecimal getVacancia() {
        return vacancia;
    }

    public void setVacancia(BigDecimal vacancia) {
        this.vacancia = vacancia;
    }

    public BigDecimal getNumeroDeCotistas() {
        return numeroDeCotistas;
    }

    public void setNumeroDeCotistas(BigDecimal numeroDeCotistas) {
        this.numeroDeCotistas = numeroDeCotistas;
    }

    public BigDecimal getCotasEmitidas() {
        return cotasEmitidas;
    }

    public void setCotasEmitidas(BigDecimal cotasEmitidas) {
        this.cotasEmitidas = cotasEmitidas;
    }
}
