package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@ToString(exclude = "fundoImobiliario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "fii_dividendo",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fii_dividendo_fundo_mes",
                        columnNames = {"fundo_imobiliario_id", "mes"}
                )
        }
)
public class FiiDividendoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mes", nullable = false)
    private LocalDate mes;

    @Column(name = "valor", precision = 19, scale = 4, nullable = false)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fundo_imobiliario_id", nullable = false)
    private FundoImobiliarioEntity fundoImobiliario;

    public void setMes(LocalDate mes) {
        this.mes = (mes == null) ? null : mes.withDayOfMonth(1);
    }

    @PrePersist
    @PreUpdate
    private void normalizeMes() {
        if (this.mes != null) this.mes = this.mes.withDayOfMonth(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiiDividendoEntity that)) return false;
        Long thisFundoId = this.fundoImobiliario != null ? this.fundoImobiliario.getId() : null;
        Long thatFundoId = that.fundoImobiliario != null ? that.fundoImobiliario.getId() : null;
        return Objects.equals(thisFundoId, thatFundoId) && Objects.equals(this.mes, that.mes);
    }

    @Override
    public int hashCode() {
        Long fundoId = fundoImobiliario != null ? fundoImobiliario.getId() : null;
        return Objects.hash(fundoId, mes);
    }
}
