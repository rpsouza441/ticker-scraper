package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoDividendo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;


@Entity
@Table(name = "dividendo", 
       indexes = {
           @Index(name = "ix_div_mes", columnList = "mes"),
           @Index(name = "ix_div_tipo", columnList = "tipo_dividendo")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_dividendo_ativo_mes_tipo_moeda", 
                           columnNames = {"ativo_id", "mes", "tipo_dividendo", "moeda"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DividendoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Chave primária simples (surrogate key)

    // O YearMonth não é um tipo padrão do JPA, então precisamos convertê-lo.
    // A forma mais simples é para uma String 'YYYY-MM'.
    @Column(name = "mes", length = 7, nullable = false)
    private String mes;

    @Column(name = "valor", precision = 18, scale = 6, nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dividendo", nullable = false)
    private TipoDividendo tipoDividendo;

    @Column(name = "moeda", length = 3, nullable = false)
    private String moeda;

    // Relação de volta para o AtivoFinanceiro ao qual este dividendo pertence.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativo_id", nullable = false)
    private AtivoFinanceiroEntity ativo;

    // Métod de conveniência para lidar com a conversão do YearMonth
    public void setMes(YearMonth mes) {
        if (mes != null) {
            this.mes = mes.toString(); // Converte '2025-09' para String
        }
    }

    public YearMonth getMes() {
        if (this.mes != null) {
            return YearMonth.parse(this.mes); // Converte a String de volta
        }
        return null;
    }
}