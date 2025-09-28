package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import br.dev.rodrigopinheiro.tickerscraper.domain.exception.DomainValidationException;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public abstract class AtivoFinanceiro {
    private Integer investidorId;
    private String ticker;
    private String nome;
    private BigDecimal precoAtual;
    private BigDecimal variacao12M;
    private BigDecimal dividendYield;
    private Instant dataAtualizacao;
    private TipoAtivo tipoAtivo;
    private List<Dividendo> dividendos = new ArrayList<>();

    public AtivoFinanceiro() {
    }

    public AtivoFinanceiro(Integer investidorId, String ticker, String nome, BigDecimal precoAtual, BigDecimal variacao12M, BigDecimal dividendYield, Instant dataAtualizacao, TipoAtivo tipoAtivo, List<Dividendo> dividendos) {
        this.investidorId = investidorId;
        this.ticker = ticker;
        this.nome = nome;
        this.precoAtual = precoAtual;
        this.variacao12M = variacao12M;
        this.dividendYield = dividendYield;
        this.dataAtualizacao = dataAtualizacao;
        this.tipoAtivo = tipoAtivo;
        this.dividendos = dividendos;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPrecoAtual() {
        return precoAtual;
    }

    public void setPrecoAtual(BigDecimal precoAtual) {
        this.precoAtual = precoAtual;
    }

    public BigDecimal getVariacao12M() {
        return variacao12M;
    }

    public void setVariacao12M(BigDecimal variacao12M) {
        this.variacao12M = variacao12M;
    }

    public BigDecimal getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(BigDecimal dividendYield) {
        this.dividendYield = dividendYield;
    }

    public Instant getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Instant dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public TipoAtivo getTipoAtivo() {
        return tipoAtivo;
    }

    public void setTipoAtivo(TipoAtivo tipoAtivo) {
        this.tipoAtivo = tipoAtivo;
    }

    public Integer getInvestidorId() {
        return investidorId;
    }

    public void setInvestidorId(Integer investidorId) {
        this.investidorId = investidorId;
    }

    // ---- Helpers de consistência ----
    public List<Dividendo> getDividendos() {
        return Collections.unmodifiableList(dividendos);
    }

    /**
     * Insere ou substitui por (mes, tipoDividendo, moeda).
     */
    public void upsertDividendo(Dividendo novo) {
        // validações de domínio
        if (novo == null) throw new DomainValidationException("dividendo obrigatório");
        if (novo.getMes() == null) throw new DomainValidationException("mes obrigatório");
        if (novo.getTipoDividendo() == null) throw new DomainValidationException("tipoDividendo obrigatório");
        if (novo.getMoeda() == null || novo.getMoeda().isBlank())
            throw new DomainValidationException("moeda obrigatória");
        if (novo.getValor() == null) throw new DomainValidationException("valor obrigatório");
        if (novo.getValor().signum() < 0) throw new DomainValidationException("valor não pode ser negativo");

        // normalizações
        novo.setMoeda(novo.getMoeda().trim().toUpperCase());
        // padronize a escala se fizer sentido para você (ex.: 6 casas decimais)
        novo.setValor(novo.getValor().stripTrailingZeros()); // ou .setScale(6, RoundingMode.HALF_UP)

        // upsert pela identidade (mes, tipo, moeda normalizada)
        dividendos.removeIf(d ->
                d.getMes().equals(novo.getMes()) &&
                        d.getTipoDividendo() == novo.getTipoDividendo() &&
                        d.getMoeda() != null &&
                        d.getMoeda().equalsIgnoreCase(novo.getMoeda())
        );
        dividendos.add(novo);

        // ordenação mais recente primeiro
        dividendos.sort(Comparator.comparing(Dividendo::getMes).reversed());
    }

    public void replaceDividendos(List<Dividendo> novos) {
        dividendos.clear();
        if (novos != null) novos.forEach(this::upsertDividendo);
    }
}
