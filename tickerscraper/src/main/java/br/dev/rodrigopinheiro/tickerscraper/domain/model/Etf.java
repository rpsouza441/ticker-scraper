package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade de domínio representando um ETF (Exchange Traded Fund).
 * 
 * <p>Um ETF é um fundo de investimento que busca replicar o desempenho de um índice
 * de mercado, permitindo diversificação com a compra de uma única cota.</p>
 */
public class Etf {

    private String ticker;
    private String nomeEtf;
    private BigDecimal valorAtual;
    private BigDecimal capitalizacao;
    private BigDecimal variacao12M;
    private BigDecimal variacao60M;
    private BigDecimal dy; // Dividend Yield
    private LocalDateTime dataAtualizacao;
    private TipoAtivo tipoAtivo = TipoAtivo.ETF;

    public Etf() {
    }

    public Etf(String ticker, String nomeEtf, BigDecimal valorAtual, BigDecimal capitalizacao, 
               BigDecimal variacao12M, BigDecimal variacao60M, BigDecimal dy, 
               LocalDateTime dataAtualizacao) {
        this.ticker = ticker;
        this.nomeEtf = nomeEtf;
        this.valorAtual = valorAtual;
        this.capitalizacao = capitalizacao;
        this.variacao12M = variacao12M;
        this.variacao60M = variacao60M;
        this.dy = dy;
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker == null ? null : ticker.trim().toUpperCase();
    }

    public String getNomeEtf() {
        return nomeEtf;
    }

    public void setNomeEtf(String nomeEtf) {
        this.nomeEtf = nomeEtf;
    }

    public BigDecimal getValorAtual() {
        return valorAtual;
    }

    public void setValorAtual(BigDecimal valorAtual) {
        this.valorAtual = valorAtual;
    }

    public BigDecimal getCapitalizacao() {
        return capitalizacao;
    }

    public void setCapitalizacao(BigDecimal capitalizacao) {
        this.capitalizacao = capitalizacao;
    }

    public BigDecimal getVariacao12M() {
        return variacao12M;
    }

    public void setVariacao12M(BigDecimal variacao12M) {
        this.variacao12M = variacao12M;
    }

    public BigDecimal getVariacao60M() {
        return variacao60M;
    }

    public void setVariacao60M(BigDecimal variacao60M) {
        this.variacao60M = variacao60M;
    }

    public BigDecimal getDy() {
        return dy;
    }

    public void setDy(BigDecimal dy) {
        this.dy = dy;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public TipoAtivo getTipoAtivo() {
        return tipoAtivo;
    }

    public void setTipoAtivo(TipoAtivo tipoAtivo) {
        this.tipoAtivo = tipoAtivo;
    }

    @Override
    public String toString() {
        return "Etf{" +
                "ticker='" + ticker + '\'' +
                ", nomeEtf='" + nomeEtf + '\'' +
                ", valorAtual=" + valorAtual +
                ", capitalizacao=" + capitalizacao +
                ", variacao12M=" + variacao12M +
                ", variacao60M=" + variacao60M +
                ", dy=" + dy +
                ", dataAtualizacao=" + dataAtualizacao +
                ", tipoAtivo=" + tipoAtivo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Etf etf = (Etf) o;

        return ticker != null ? ticker.equals(etf.ticker) : etf.ticker == null;
    }

    @Override
    public int hashCode() {
        return ticker != null ? ticker.hashCode() : 0;
    }
}