package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import java.math.BigDecimal;

public class Acao {

    private String ticker;
    private String nomeEmpresa;
    private String setor;
    private String segmento;
    private String segmentoListagem;
    private String precoAtual;
    private double variacao12M;
    private BigDecimal valorMercado;
    private BigDecimal valorFirma;
    private BigDecimal patrimonioLiquido;
    private BigDecimal numeroTotalPapeis;
    private BigDecimal ativos;
    private BigDecimal ativoCirculantes;
    private BigDecimal dividaBruta;
    private BigDecimal dividaLiquida;
    private BigDecimal disponibilidade;
    private double freeFloat;
    private double tagAlong;
    private BigDecimal liquidezMediaDiaria;
    private double pL;
    private double psr;
    private double pVp;
    private double dividendYeld;
    private double payout;
    private double margemLiquida;
    private double margemBruta;
    private double margemEbit;
    private double margemEbitda;
    private double evEbitda;
    private double evEbit;
    private double pEbitda;
    private double pAtivo;
    private double pCapitaldeGiro;
    private double pAtivoCirculanteLiquido;
    private double vpa;
    private double lpa;
    private double giroAtivos;
    private double roe;
    private double roic;
    private double roa;
    private double dividaLiquidaPatrimonio;
    private double dividaLiquidaEbitda;
    private double dividaLiquidaEbit;
    private double dividaBrutaPatrimonio;
    private double patrimonioAtivos;
    private double passivosAtivos;
    private double liquidezCorrente;
    private double cagrReceitasCincoAnos;
    private double cagrLucrosCincoAnos;

    public Acao() {
    }

    public Acao(String ticker, String nomeEmpresa, String setor, String segmento, String segmentoListagem, String precoAtual, double variacao12M, BigDecimal valorMercado, BigDecimal valorFirma, BigDecimal patrimonioLiquido, BigDecimal numeroTotalPapeis, BigDecimal ativos, BigDecimal ativoCirculantes, BigDecimal dividaBruta, BigDecimal dividaLiquida, BigDecimal disponibilidade, double freeFloat, double tagAlong, BigDecimal liquidezMediaDiaria, double pL, double psr, double pVp, double dividendYeld, double payout, double margemLiquida, double margemBruta, double margemEbit, double margemEbitda, double evEbitda, double evEbit, double pEbitda, double pAtivo, double pCapitaldeGiro, double pAtivoCirculanteLiquido, double vpa, double lpa, double giroAtivos, double roe, double roic, double roa, double dividaLiquidaPatrimonio, double dividaLiquidaEbitda, double dividaLiquidaEbit, double dividaBrutaPatrimonio, double patrimonioAtivos, double passivosAtivos, double liquidezCorrente, double cagrReceitasCincoAnos, double cagrLucrosCincoAnos) {
        this.ticker = ticker;
        this.nomeEmpresa = nomeEmpresa;
        this.setor = setor;
        this.segmento = segmento;
        this.segmentoListagem = segmentoListagem;
        this.precoAtual = precoAtual;
        this.variacao12M = variacao12M;
        this.valorMercado = valorMercado;
        this.valorFirma = valorFirma;
        this.patrimonioLiquido = patrimonioLiquido;
        this.numeroTotalPapeis = numeroTotalPapeis;
        this.ativos = ativos;
        this.ativoCirculantes = ativoCirculantes;
        this.dividaBruta = dividaBruta;
        this.dividaLiquida = dividaLiquida;
        this.disponibilidade = disponibilidade;
        this.freeFloat = freeFloat;
        this.tagAlong = tagAlong;
        this.liquidezMediaDiaria = liquidezMediaDiaria;
        this.pL = pL;
        this.psr = psr;
        this.pVp = pVp;
        this.dividendYeld = dividendYeld;
        this.payout = payout;
        this.margemLiquida = margemLiquida;
        this.margemBruta = margemBruta;
        this.margemEbit = margemEbit;
        this.margemEbitda = margemEbitda;
        this.evEbitda = evEbitda;
        this.evEbit = evEbit;
        this.pEbitda = pEbitda;
        this.pAtivo = pAtivo;
        this.pCapitaldeGiro = pCapitaldeGiro;
        this.pAtivoCirculanteLiquido = pAtivoCirculanteLiquido;
        this.vpa = vpa;
        this.lpa = lpa;
        this.giroAtivos = giroAtivos;
        this.roe = roe;
        this.roic = roic;
        this.roa = roa;
        this.dividaLiquidaPatrimonio = dividaLiquidaPatrimonio;
        this.dividaLiquidaEbitda = dividaLiquidaEbitda;
        this.dividaLiquidaEbit = dividaLiquidaEbit;
        this.dividaBrutaPatrimonio = dividaBrutaPatrimonio;
        this.patrimonioAtivos = patrimonioAtivos;
        this.passivosAtivos = passivosAtivos;
        this.liquidezCorrente = liquidezCorrente;
        this.cagrReceitasCincoAnos = cagrReceitasCincoAnos;
        this.cagrLucrosCincoAnos = cagrLucrosCincoAnos;
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

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

    public String getSegmentoListagem() {
        return segmentoListagem;
    }

    public void setSegmentoListagem(String segmentoListagem) {
        this.segmentoListagem = segmentoListagem;
    }

    public String getPrecoAtual() {
        return precoAtual;
    }

    public void setPrecoAtual(String precoAtual) {
        this.precoAtual = precoAtual;
    }

    public double getVariacao12M() {
        return variacao12M;
    }

    public void setVariacao12M(double variacao12M) {
        this.variacao12M = variacao12M;
    }

    public BigDecimal getValorMercado() {
        return valorMercado;
    }

    public void setValorMercado(BigDecimal valorMercado) {
        this.valorMercado = valorMercado;
    }

    public BigDecimal getValorFirma() {
        return valorFirma;
    }

    public void setValorFirma(BigDecimal valorFirma) {
        this.valorFirma = valorFirma;
    }

    public BigDecimal getPatrimonioLiquido() {
        return patrimonioLiquido;
    }

    public void setPatrimonioLiquido(BigDecimal patrimonioLiquido) {
        this.patrimonioLiquido = patrimonioLiquido;
    }

    public BigDecimal getNumeroTotalPapeis() {
        return numeroTotalPapeis;
    }

    public void setNumeroTotalPapeis(BigDecimal numeroTotalPapeis) {
        this.numeroTotalPapeis = numeroTotalPapeis;
    }

    public BigDecimal getAtivos() {
        return ativos;
    }

    public void setAtivos(BigDecimal ativos) {
        this.ativos = ativos;
    }

    public BigDecimal getAtivoCirculantes() {
        return ativoCirculantes;
    }

    public void setAtivoCirculantes(BigDecimal ativoCirculantes) {
        this.ativoCirculantes = ativoCirculantes;
    }

    public BigDecimal getDividaBruta() {
        return dividaBruta;
    }

    public void setDividaBruta(BigDecimal dividaBruta) {
        this.dividaBruta = dividaBruta;
    }

    public BigDecimal getDividaLiquida() {
        return dividaLiquida;
    }

    public void setDividaLiquida(BigDecimal dividaLiquida) {
        this.dividaLiquida = dividaLiquida;
    }

    public BigDecimal getDisponibilidade() {
        return disponibilidade;
    }

    public void setDisponibilidade(BigDecimal disponibilidade) {
        this.disponibilidade = disponibilidade;
    }

    public double getFreeFloat() {
        return freeFloat;
    }

    public void setFreeFloat(double freeFloat) {
        this.freeFloat = freeFloat;
    }

    public double getTagAlong() {
        return tagAlong;
    }

    public void setTagAlong(double tagAlong) {
        this.tagAlong = tagAlong;
    }

    public BigDecimal getLiquidezMediaDiaria() {
        return liquidezMediaDiaria;
    }

    public void setLiquidezMediaDiaria(BigDecimal liquidezMediaDiaria) {
        this.liquidezMediaDiaria = liquidezMediaDiaria;
    }

    public double getpL() {
        return pL;
    }

    public void setpL(double pL) {
        this.pL = pL;
    }

    public double getPsr() {
        return psr;
    }

    public void setPsr(double psr) {
        this.psr = psr;
    }

    public double getpVp() {
        return pVp;
    }

    public void setpVp(double pVp) {
        this.pVp = pVp;
    }

    public double getDividendYeld() {
        return dividendYeld;
    }

    public void setDividendYeld(double dividendYeld) {
        this.dividendYeld = dividendYeld;
    }

    public double getPayout() {
        return payout;
    }

    public void setPayout(double payout) {
        this.payout = payout;
    }

    public double getMargemLiquida() {
        return margemLiquida;
    }

    public void setMargemLiquida(double margemLiquida) {
        this.margemLiquida = margemLiquida;
    }

    public double getMargemBruta() {
        return margemBruta;
    }

    public void setMargemBruta(double margemBruta) {
        this.margemBruta = margemBruta;
    }

    public double getMargemEbit() {
        return margemEbit;
    }

    public void setMargemEbit(double margemEbit) {
        this.margemEbit = margemEbit;
    }

    public double getMargemEbitda() {
        return margemEbitda;
    }

    public void setMargemEbitda(double margemEbitda) {
        this.margemEbitda = margemEbitda;
    }

    public double getEvEbitda() {
        return evEbitda;
    }

    public void setEvEbitda(double evEbitda) {
        this.evEbitda = evEbitda;
    }

    public double getEvEbit() {
        return evEbit;
    }

    public void setEvEbit(double evEbit) {
        this.evEbit = evEbit;
    }

    public double getpEbitda() {
        return pEbitda;
    }

    public void setpEbitda(double pEbitda) {
        this.pEbitda = pEbitda;
    }

    public double getpAtivo() {
        return pAtivo;
    }

    public void setpAtivo(double pAtivo) {
        this.pAtivo = pAtivo;
    }

    public double getpCapitaldeGiro() {
        return pCapitaldeGiro;
    }

    public void setpCapitaldeGiro(double pCapitaldeGiro) {
        this.pCapitaldeGiro = pCapitaldeGiro;
    }

    public double getpAtivoCirculanteLiquido() {
        return pAtivoCirculanteLiquido;
    }

    public void setpAtivoCirculanteLiquido(double pAtivoCirculanteLiquido) {
        this.pAtivoCirculanteLiquido = pAtivoCirculanteLiquido;
    }

    public double getVpa() {
        return vpa;
    }

    public void setVpa(double vpa) {
        this.vpa = vpa;
    }

    public double getLpa() {
        return lpa;
    }

    public void setLpa(double lpa) {
        this.lpa = lpa;
    }

    public double getGiroAtivos() {
        return giroAtivos;
    }

    public void setGiroAtivos(double giroAtivos) {
        this.giroAtivos = giroAtivos;
    }

    public double getRoe() {
        return roe;
    }

    public void setRoe(double roe) {
        this.roe = roe;
    }

    public double getRoic() {
        return roic;
    }

    public void setRoic(double roic) {
        this.roic = roic;
    }

    public double getRoa() {
        return roa;
    }

    public void setRoa(double roa) {
        this.roa = roa;
    }

    public double getDividaLiquidaPatrimonio() {
        return dividaLiquidaPatrimonio;
    }

    public void setDividaLiquidaPatrimonio(double dividaLiquidaPatrimonio) {
        this.dividaLiquidaPatrimonio = dividaLiquidaPatrimonio;
    }

    public double getDividaLiquidaEbitda() {
        return dividaLiquidaEbitda;
    }

    public void setDividaLiquidaEbitda(double dividaLiquidaEbitda) {
        this.dividaLiquidaEbitda = dividaLiquidaEbitda;
    }

    public double getDividaLiquidaEbit() {
        return dividaLiquidaEbit;
    }

    public void setDividaLiquidaEbit(double dividaLiquidaEbit) {
        this.dividaLiquidaEbit = dividaLiquidaEbit;
    }

    public double getDividaBrutaPatrimonio() {
        return dividaBrutaPatrimonio;
    }

    public void setDividaBrutaPatrimonio(double dividaBrutaPatrimonio) {
        this.dividaBrutaPatrimonio = dividaBrutaPatrimonio;
    }

    public double getPatrimonioAtivos() {
        return patrimonioAtivos;
    }

    public void setPatrimonioAtivos(double patrimonioAtivos) {
        this.patrimonioAtivos = patrimonioAtivos;
    }

    public double getPassivosAtivos() {
        return passivosAtivos;
    }

    public void setPassivosAtivos(double passivosAtivos) {
        this.passivosAtivos = passivosAtivos;
    }

    public double getLiquidezCorrente() {
        return liquidezCorrente;
    }

    public void setLiquidezCorrente(double liquidezCorrente) {
        this.liquidezCorrente = liquidezCorrente;
    }

    public double getCagrReceitasCincoAnos() {
        return cagrReceitasCincoAnos;
    }

    public void setCagrReceitasCincoAnos(double cagrReceitasCincoAnos) {
        this.cagrReceitasCincoAnos = cagrReceitasCincoAnos;
    }

    public double getCagrLucrosCincoAnos() {
        return cagrLucrosCincoAnos;
    }

    public void setCagrLucrosCincoAnos(double cagrLucrosCincoAnos) {
        this.cagrLucrosCincoAnos = cagrLucrosCincoAnos;
    }
}
