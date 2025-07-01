package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import java.math.BigDecimal;

public class Acao {

    private String ticker;
    private String nomeEmpresa;
    private String setor;
    private String segmento;
    private String segmentoListagem;
    private String precoAtual;
    private BigDecimal variacao12M;
    private BigDecimal valorMercado;
    private BigDecimal valorFirma;
    private BigDecimal patrimonioLiquido;
    private BigDecimal numeroTotalPapeis;
    private BigDecimal ativos;
    private BigDecimal ativoCirculantes;
    private BigDecimal dividaBruta;
    private BigDecimal dividaLiquida;
    private BigDecimal disponibilidade;
    private BigDecimal freeFloat;
    private BigDecimal tagAlong;
    private BigDecimal liquidezMediaDiaria;
    private BigDecimal pL;
    private BigDecimal psr;
    private BigDecimal pVp;
    private BigDecimal dividendYeld;
    private BigDecimal payout;
    private BigDecimal margemLiquida;
    private BigDecimal margemBruta;
    private BigDecimal margemEbit;
    private BigDecimal margemEbitda;
    private BigDecimal evEbitda;
    private BigDecimal evEbit;
    private BigDecimal pEbitda;
    private BigDecimal pAtivo;
    private BigDecimal pCapitaldeGiro;
    private BigDecimal pAtivoCirculanteLiquido;
    private BigDecimal vpa;
    private BigDecimal lpa;
    private BigDecimal giroAtivos;
    private BigDecimal roe;
    private BigDecimal roic;
    private BigDecimal roa;
    private BigDecimal dividaLiquidaPatrimonio;
    private BigDecimal dividaLiquidaEbitda;
    private BigDecimal dividaLiquidaEbit;
    private BigDecimal dividaBrutaPatrimonio;
    private BigDecimal patrimonioAtivos;
    private BigDecimal passivosAtivos;
    private BigDecimal liquidezCorrente;
    private BigDecimal cagrReceitasCincoAnos;
    private BigDecimal cagrLucrosCincoAnos;

    public Acao() {
    }

    public Acao(String ticker, String nomeEmpresa, String setor, String segmento, String segmentoListagem, String precoAtual, BigDecimal variacao12M, BigDecimal valorMercado, BigDecimal valorFirma, BigDecimal patrimonioLiquido, BigDecimal numeroTotalPapeis, BigDecimal ativos, BigDecimal ativoCirculantes, BigDecimal dividaBruta, BigDecimal dividaLiquida, BigDecimal disponibilidade, BigDecimal freeFloat, BigDecimal tagAlong, BigDecimal liquidezMediaDiaria, BigDecimal pL, BigDecimal psr, BigDecimal pVp, BigDecimal dividendYeld, BigDecimal payout, BigDecimal margemLiquida, BigDecimal margemBruta, BigDecimal margemEbit, BigDecimal margemEbitda, BigDecimal evEbitda, BigDecimal evEbit, BigDecimal pEbitda, BigDecimal pAtivo, BigDecimal pCapitaldeGiro, BigDecimal pAtivoCirculanteLiquido, BigDecimal vpa, BigDecimal lpa, BigDecimal giroAtivos, BigDecimal roe, BigDecimal roic, BigDecimal roa, BigDecimal dividaLiquidaPatrimonio, BigDecimal dividaLiquidaEbitda, BigDecimal dividaLiquidaEbit, BigDecimal dividaBrutaPatrimonio, BigDecimal patrimonioAtivos, BigDecimal passivosAtivos, BigDecimal liquidezCorrente, BigDecimal cagrReceitasCincoAnos, BigDecimal cagrLucrosCincoAnos) {
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

    public BigDecimal getVariacao12M() {
        return variacao12M;
    }

    public void setVariacao12M(BigDecimal variacao12M) {
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

    public BigDecimal getFreeFloat() {
        return freeFloat;
    }

    public void setFreeFloat(BigDecimal freeFloat) {
        this.freeFloat = freeFloat;
    }

    public BigDecimal getTagAlong() {
        return tagAlong;
    }

    public void setTagAlong(BigDecimal tagAlong) {
        this.tagAlong = tagAlong;
    }

    public BigDecimal getLiquidezMediaDiaria() {
        return liquidezMediaDiaria;
    }

    public void setLiquidezMediaDiaria(BigDecimal liquidezMediaDiaria) {
        this.liquidezMediaDiaria = liquidezMediaDiaria;
    }

    public BigDecimal getpL() {
        return pL;
    }

    public void setpL(BigDecimal pL) {
        this.pL = pL;
    }

    public BigDecimal getPsr() {
        return psr;
    }

    public void setPsr(BigDecimal psr) {
        this.psr = psr;
    }

    public BigDecimal getpVp() {
        return pVp;
    }

    public void setpVp(BigDecimal pVp) {
        this.pVp = pVp;
    }

    public BigDecimal getDividendYeld() {
        return dividendYeld;
    }

    public void setDividendYeld(BigDecimal dividendYeld) {
        this.dividendYeld = dividendYeld;
    }

    public BigDecimal getPayout() {
        return payout;
    }

    public void setPayout(BigDecimal payout) {
        this.payout = payout;
    }

    public BigDecimal getMargemLiquida() {
        return margemLiquida;
    }

    public void setMargemLiquida(BigDecimal margemLiquida) {
        this.margemLiquida = margemLiquida;
    }

    public BigDecimal getMargemBruta() {
        return margemBruta;
    }

    public void setMargemBruta(BigDecimal margemBruta) {
        this.margemBruta = margemBruta;
    }

    public BigDecimal getMargemEbit() {
        return margemEbit;
    }

    public void setMargemEbit(BigDecimal margemEbit) {
        this.margemEbit = margemEbit;
    }

    public BigDecimal getMargemEbitda() {
        return margemEbitda;
    }

    public void setMargemEbitda(BigDecimal margemEbitda) {
        this.margemEbitda = margemEbitda;
    }

    public BigDecimal getEvEbitda() {
        return evEbitda;
    }

    public void setEvEbitda(BigDecimal evEbitda) {
        this.evEbitda = evEbitda;
    }

    public BigDecimal getEvEbit() {
        return evEbit;
    }

    public void setEvEbit(BigDecimal evEbit) {
        this.evEbit = evEbit;
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

    public BigDecimal getpCapitaldeGiro() {
        return pCapitaldeGiro;
    }

    public void setpCapitaldeGiro(BigDecimal pCapitaldeGiro) {
        this.pCapitaldeGiro = pCapitaldeGiro;
    }

    public BigDecimal getpAtivoCirculanteLiquido() {
        return pAtivoCirculanteLiquido;
    }

    public void setpAtivoCirculanteLiquido(BigDecimal pAtivoCirculanteLiquido) {
        this.pAtivoCirculanteLiquido = pAtivoCirculanteLiquido;
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

    public BigDecimal getGiroAtivos() {
        return giroAtivos;
    }

    public void setGiroAtivos(BigDecimal giroAtivos) {
        this.giroAtivos = giroAtivos;
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

    public BigDecimal getDividaLiquidaPatrimonio() {
        return dividaLiquidaPatrimonio;
    }

    public void setDividaLiquidaPatrimonio(BigDecimal dividaLiquidaPatrimonio) {
        this.dividaLiquidaPatrimonio = dividaLiquidaPatrimonio;
    }

    public BigDecimal getDividaLiquidaEbitda() {
        return dividaLiquidaEbitda;
    }

    public void setDividaLiquidaEbitda(BigDecimal dividaLiquidaEbitda) {
        this.dividaLiquidaEbitda = dividaLiquidaEbitda;
    }

    public BigDecimal getDividaLiquidaEbit() {
        return dividaLiquidaEbit;
    }

    public void setDividaLiquidaEbit(BigDecimal dividaLiquidaEbit) {
        this.dividaLiquidaEbit = dividaLiquidaEbit;
    }

    public BigDecimal getDividaBrutaPatrimonio() {
        return dividaBrutaPatrimonio;
    }

    public void setDividaBrutaPatrimonio(BigDecimal dividaBrutaPatrimonio) {
        this.dividaBrutaPatrimonio = dividaBrutaPatrimonio;
    }

    public BigDecimal getPatrimonioAtivos() {
        return patrimonioAtivos;
    }

    public void setPatrimonioAtivos(BigDecimal patrimonioAtivos) {
        this.patrimonioAtivos = patrimonioAtivos;
    }

    public BigDecimal getPassivosAtivos() {
        return passivosAtivos;
    }

    public void setPassivosAtivos(BigDecimal passivosAtivos) {
        this.passivosAtivos = passivosAtivos;
    }

    public BigDecimal getLiquidezCorrente() {
        return liquidezCorrente;
    }

    public void setLiquidezCorrente(BigDecimal liquidezCorrente) {
        this.liquidezCorrente = liquidezCorrente;
    }

    public BigDecimal getCagrReceitasCincoAnos() {
        return cagrReceitasCincoAnos;
    }

    public void setCagrReceitasCincoAnos(BigDecimal cagrReceitasCincoAnos) {
        this.cagrReceitasCincoAnos = cagrReceitasCincoAnos;
    }

    public BigDecimal getCagrLucrosCincoAnos() {
        return cagrLucrosCincoAnos;
    }

    public void setCagrLucrosCincoAnos(BigDecimal cagrLucrosCincoAnos) {
        this.cagrLucrosCincoAnos = cagrLucrosCincoAnos;
    }
}
