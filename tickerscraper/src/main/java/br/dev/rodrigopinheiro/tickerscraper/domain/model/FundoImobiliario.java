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
    private BigDecimal numeroDeCotitas;
    private BigDecimal cotasEmitidas;



}
