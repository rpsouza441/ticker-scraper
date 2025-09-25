package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;


import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.shared.YearMonthAttributeConverter;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoDividendo;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Entity 1:1 com o domínio:
 *   mes (YearMonth), valor (BigDecimal), tipoDividendo (enum), moeda corrente (String)
 * Sem campo adicional. Chave composta: (mes, tipo_dividendo).
 */
@Entity
@Table(name = "dividendo", indexes = {
        @Index(name = "ix_div_mes", columnList = "mes"),
        @Index(name = "ix_div_tipo", columnList = "tipo_dividendo")
})
@IdClass(DividendoEntity.DividendoPk.class)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Access(AccessType.FIELD)
public class DividendoEntity {

    // ===== PK composta =====
    @Id
    @Convert(converter = YearMonthAttributeConverter.class)
    @Column(name = "mes", length = 7, nullable = false) // "yyyy-MM"
    private YearMonth mes;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dividendo", length = 20, nullable = false)
    private TipoDividendo tipoDividendo;

    // ===== Demais campos (1:1 com o domínio) =====
    @Column(name = "valor", precision = 18, scale = 6, nullable = false)
    private BigDecimal valor;

    @Column(name = "moeda", length = 3, nullable = false)
    private String moeda;

    // ===== PK class (inner) =====
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class DividendoPk implements Serializable {
        private YearMonth mes;
        private TipoDividendo tipoDividendo;
    }
}