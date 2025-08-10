package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "fii_dividendo")
@Getter
@Setter
@ToString(exclude = "fundoImobiliario")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiiDividendoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mes")
    private LocalDate mes;

    @Column(name = "valor", precision = 12, scale = 4)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fundo_imobiliario_id", nullable = false)
    private FundoImobiliarioEntity fundoImobiliario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FiiDividendoEntity that = (FiiDividendoEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(mes, that.mes) && Objects.equals(valor, that.valor) && Objects.equals(fundoImobiliario, that.fundoImobiliario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mes, valor, fundoImobiliario);
    }
}