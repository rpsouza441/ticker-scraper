package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.converter.ParidadeMethodAttributeConverter;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.ParidadeMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bdr_paridade")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BdrParidadeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bdr_id", unique = true)
    private BdrEntity bdr;

    @Column(name = "value")
    private Integer value;

    @Convert(converter = ParidadeMethodAttributeConverter.class)
    @Column(name = "method", columnDefinition = "paridade_method_enum")
    private ParidadeMethod method;

    @Column(name = "confidence", precision = 4, scale = 3)
    private BigDecimal confidence;

    @Column(name = "last_verified_at")
    private Instant lastVerifiedAt;

    @Column(name = "raw", columnDefinition = "TEXT")
    private String raw;
}
