package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class FiiResponseDTO {
    public String ticker;
    public String nomeEmpresa;
    public String razaoSocial;
    public String cnpj;
    public String publicoAlvo;
    public String mandato;
    public String segmento;
    public String tipoDeFundo;
    public String prazoDeDuracao;
    public String tipoDeGestao;

    public BigDecimal taxaDeAdministracao;
    public BigDecimal ultimoRendimento;

    public BigDecimal cotacao;
    public BigDecimal valorDeMercado;
    public BigDecimal pvp;
    public BigDecimal dividendYield;
    public BigDecimal liquidezDiaria;
    public BigDecimal valorPatrimonial;
    public BigDecimal valorPatrimonialPorCota;
    public BigDecimal vacancia;
    public BigDecimal numeroDeCotistas;
    public BigDecimal cotasEmitidas;

    public LocalDateTime dataAtualizacao;

    public List<FiiDividendoResponseDTO> dividendos;
}
