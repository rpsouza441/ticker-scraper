package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Acao {

    private String ticker;
    private String nomeEmpresa;
    private String setor;
    private String segmento;
    private String segmentoListagem;
    private BigDecimal precoAtual;
    private BigDecimal variacao12M;
    private BigDecimal valorMercado;
    private BigDecimal valorFirma;
    private BigDecimal patrimonioLiquido;
    private BigDecimal numeroTotalPapeis;
    private BigDecimal ativos;
    private BigDecimal ativoCirculantes;
    private BigDecimal dividaBruta;
    private BigDecimal dividaLiquida;
    private String disponibilidade;
    private BigDecimal freeFloat;
    private BigDecimal tagAlong;
    private BigDecimal liquidezMediaDiaria;
    private BigDecimal pl;
    private BigDecimal psr;
    private BigDecimal pvp;
    private BigDecimal dividendYield;
    private BigDecimal payout;
    private BigDecimal margemLiquida;
    private BigDecimal margemBruta;
    private BigDecimal margemEbit;
    private BigDecimal margemEbitda;
    private BigDecimal evEbitda;
    private BigDecimal evEbit;
    private BigDecimal pebitda;
    private BigDecimal pativo;
    private BigDecimal pcapitaldeGiro;
    private BigDecimal pativoCirculanteLiquido;
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
    private LocalDateTime dataAtualizacao;

    public Acao() {
    }

    public Acao(String ticker,
                String nomeEmpresa,
                String setor,
                String segmento, String segmentoListagem, BigDecimal precoAtual, BigDecimal variacao12M, BigDecimal valorMercado, BigDecimal valorFirma, BigDecimal patrimonioLiquido, BigDecimal numeroTotalPapeis, BigDecimal ativos, BigDecimal ativoCirculantes, BigDecimal dividaBruta, BigDecimal dividaLiquida, String disponibilidade, BigDecimal freeFloat, BigDecimal tagAlong, BigDecimal liquidezMediaDiaria, BigDecimal pl, BigDecimal psr, BigDecimal pvp, BigDecimal dividendYield, BigDecimal payout, BigDecimal margemLiquida, BigDecimal margemBruta, BigDecimal margemEbit, BigDecimal margemEbitda, BigDecimal evEbitda, BigDecimal evEbit, BigDecimal pebitda, BigDecimal pativo, BigDecimal pcapitaldeGiro, BigDecimal pativoCirculanteLiquido, BigDecimal vpa, BigDecimal lpa, BigDecimal giroAtivos, BigDecimal roe, BigDecimal roic, BigDecimal roa, BigDecimal dividaLiquidaPatrimonio, BigDecimal dividaLiquidaEbitda, BigDecimal dividaLiquidaEbit, BigDecimal dividaBrutaPatrimonio, BigDecimal patrimonioAtivos, BigDecimal passivosAtivos, BigDecimal liquidezCorrente, BigDecimal cagrReceitasCincoAnos, BigDecimal cagrLucrosCincoAnos, LocalDateTime dataAtualizacao) {
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
        this.pl = pl;
        this.psr = psr;
        this.pvp = pvp;
        this.dividendYield = dividendYield;
        this.payout = payout;
        this.margemLiquida = margemLiquida;
        this.margemBruta = margemBruta;
        this.margemEbit = margemEbit;
        this.margemEbitda = margemEbitda;
        this.evEbitda = evEbitda;
        this.evEbit = evEbit;
        this.pebitda = pebitda;
        this.pativo = pativo;
        this.pcapitaldeGiro = pcapitaldeGiro;
        this.pativoCirculanteLiquido = pativoCirculanteLiquido;
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
        this.dataAtualizacao = dataAtualizacao;
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

    public String getDisponibilidade() {
        return disponibilidade;
    }

    public void setDisponibilidade(String disponibilidade) {
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

    public BigDecimal getPl() {
        return pl;
    }

    public void setPl(BigDecimal pl) {
        this.pl = pl;
    }

    public BigDecimal getPsr() {
        return psr;
    }

    public void setPsr(BigDecimal psr) {
        this.psr = psr;
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

    public BigDecimal getPebitda() {
        return pebitda;
    }

    public void setPebitda(BigDecimal pebitda) {
        this.pebitda = pebitda;
    }

    public BigDecimal getPativo() {
        return pativo;
    }

    public void setPativo(BigDecimal pativo) {
        this.pativo = pativo;
    }

    public BigDecimal getPcapitaldeGiro() {
        return pcapitaldeGiro;
    }

    public void setPcapitaldeGiro(BigDecimal pcapitaldeGiro) {
        this.pcapitaldeGiro = pcapitaldeGiro;
    }

    public BigDecimal getPativoCirculanteLiquido() {
        return pativoCirculanteLiquido;
    }

    public void setPativoCirculanteLiquido(BigDecimal pativoCirculanteLiquido) {
        this.pativoCirculanteLiquido = pativoCirculanteLiquido;
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

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}