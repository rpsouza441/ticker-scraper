package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "fundo_imobiliario")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundoImobiliarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticker", unique = true, nullable = false)
    private String ticker;

    @Column(name = "nome_empresa")
    private String nomeEmpresa;

    @Column(name = "razao_social")
    private String razaoSocial;

    @Column(name = "cnpj")
    private String cnpj;

    @Column(name = "publico_alvo")
    private String publicoAlvo;

    @Column(name = "mandato")
    private String mandato;

    @Column(name = "segmento")
    private String segmento;

    @Column(name = "tipo_de_fundo")
    private String tipoDeFundo;

    @Column(name = "prazo_de_duracao")
    private String prazoDeDuracao;

    @Column(name = "tipo_de_gestao")
    private String tipoDeGestao;

    @Column(name = "taxa_de_administracao")
    private BigDecimal taxaDeAdministracao;

    @Column(name = "ultimo_rendimento")
    private BigDecimal ultimoRendimento;

    @Column(name = "cotacao")
    private BigDecimal cotacao;

    @Column(name = "valor_de_mercado")
    private BigDecimal valorDeMercado;

    @Column(name = "pvp")
    private BigDecimal pvp;

    @Column(name = "dividend_yield")
    private BigDecimal dividendYield;

    @Column(name = "liquidez_diaria")
    private BigDecimal liquidezDiaria;

    @Column(name = "valor_patrimonial")
    private BigDecimal valorPatrimonial;

    @Column(name = "valor_patrimonial_por_cota")
    private BigDecimal valorPatrimonialPorCota;

    @Column(name = "vacancia")
    private BigDecimal vacancia;

    @Column(name = "numero_de_cotistas")
    private BigDecimal numeroDeCotistas;

    @Column(name = "cotas_emitidas")
    private BigDecimal cotasEmitidas;

    // --- Relacionamento Um-para-Muitos ---
    // Um Fundo Imobiliário tem Muitos dividendos.
    @OneToMany(
            mappedBy = "fundoImobiliario", // Mapeado pelo campo "fundoImobiliario" na entidade FiiDividendoEntity
            cascade = CascadeType.ALL,    // Salva, atualiza e remove os dividendos junto com o fundo
            orphanRemoval = true          // Remove dividendos que não estão mais associados a este fundo
    )
    @Builder.Default // Garante que o Lombok Builder inicialize a lista
    private List<FiiDividendoEntity> fiiDividendos = new ArrayList<>();

    // --- Metadados de Persistência ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_brutos_json", columnDefinition = "JSONB")
    private String dadosBrutosJson;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FundoImobiliarioEntity that = (FundoImobiliarioEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(ticker, that.ticker) && Objects.equals(nomeEmpresa, that.nomeEmpresa) && Objects.equals(razaoSocial, that.razaoSocial) && Objects.equals(cnpj, that.cnpj) && Objects.equals(publicoAlvo, that.publicoAlvo) && Objects.equals(mandato, that.mandato) && Objects.equals(segmento, that.segmento) && Objects.equals(tipoDeFundo, that.tipoDeFundo) && Objects.equals(prazoDeDuracao, that.prazoDeDuracao) && Objects.equals(tipoDeGestao, that.tipoDeGestao) && Objects.equals(taxaDeAdministracao, that.taxaDeAdministracao) && Objects.equals(ultimoRendimento, that.ultimoRendimento) && Objects.equals(cotacao, that.cotacao) && Objects.equals(valorDeMercado, that.valorDeMercado) && Objects.equals(pvp, that.pvp) && Objects.equals(dividendYield, that.dividendYield) && Objects.equals(liquidezDiaria, that.liquidezDiaria) && Objects.equals(valorPatrimonial, that.valorPatrimonial) && Objects.equals(valorPatrimonialPorCota, that.valorPatrimonialPorCota) && Objects.equals(vacancia, that.vacancia) && Objects.equals(numeroDeCotistas, that.numeroDeCotistas) && Objects.equals(cotasEmitidas, that.cotasEmitidas) && Objects.equals(fiiDividendos, that.fiiDividendos) && Objects.equals(dadosBrutosJson, that.dadosBrutosJson) && Objects.equals(dataAtualizacao, that.dataAtualizacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ticker, nomeEmpresa, razaoSocial, cnpj, publicoAlvo, mandato, segmento, tipoDeFundo, prazoDeDuracao, tipoDeGestao, taxaDeAdministracao, ultimoRendimento, cotacao, valorDeMercado, pvp, dividendYield, liquidezDiaria, valorPatrimonial, valorPatrimonialPorCota, vacancia, numeroDeCotistas, cotasEmitidas, fiiDividendos, dadosBrutosJson, dataAtualizacao);
    }
}
