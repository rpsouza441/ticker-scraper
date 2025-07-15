package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

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

    // Armazenamos YearMonth como um DATE no banco. O primeiro dia do mês é uma boa convenção.
    @Column(name = "data_referencia")
    private LocalDate dataDeReferencia;

    @Column(name = "valor_pago", precision = 12, scale = 4)
    private BigDecimal valorPago;

    // --- Relacionamento Muitos-para-Um ---
    // Muitos dividendos pertencem a Um fundo imobiliário.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fundo_imobiliario_id", nullable = false)
    private FundoImobiliarioEntity fundoImobiliario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FiiDividendoEntity that = (FiiDividendoEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(dataDeReferencia, that.dataDeReferencia) && Objects.equals(valorPago, that.valorPago) && Objects.equals(fundoImobiliario, that.fundoImobiliario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dataDeReferencia, valorPago, fundoImobiliario);
    }
}