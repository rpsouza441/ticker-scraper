package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "etf")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ticker", unique = true, nullable = false)
    private String ticker;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ativo", length = 20, nullable = false)
    @Builder.Default
    private TipoAtivo tipoAtivo = TipoAtivo.ETF;
    
    @Column(name = "nome_etf")
    private String nomeEtf;
    
    @Column(name = "valor_atual")
    private BigDecimal valorAtual;
    
    @Column(name = "capitalizacao")
    private BigDecimal capitalizacao;
    
    @Column(name = "variacao_12m")
    private BigDecimal variacao12M;
    
    @Column(name = "variacao_60m")
    private BigDecimal variacao60M;
    
    @Column(name = "dy")
    private BigDecimal dy;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_brutos_json", columnDefinition = "JSONB")
    private String dadosBrutosJson;
    
    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        EtfEntity that = (EtfEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }
    
    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}