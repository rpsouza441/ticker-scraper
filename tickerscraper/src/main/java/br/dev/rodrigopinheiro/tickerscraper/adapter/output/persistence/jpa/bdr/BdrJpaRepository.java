package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BdrJpaRepository extends JpaRepository<BdrEntity, Long> {

    Optional<BdrEntity> findByTicker(String ticker);

    boolean existsByTicker(String ticker);

    @EntityGraph(attributePaths = {
            "priceSeries",
            "dividendYears",
            "historicalIndicators",
            "dreYears",
            "bpYears",
            "fcYears",
            "currentIndicators",
            "paridade",
            "marketCap"
    })
    @Query("""
            select distinct b
            from BdrEntity b
            where b.ticker = :ticker
            """)
    Optional<BdrEntity> findDetailedByTicker(@Param("ticker") String ticker);

    @EntityGraph(attributePaths = {
            "priceSeries",
            "dividendYears",
            "historicalIndicators",
            "dreYears",
            "bpYears",
            "fcYears",
            "currentIndicators",
            "paridade",
            "marketCap"
    })
    @Override
    Page<BdrEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {
            "priceSeries",
            "dividendYears",
            "historicalIndicators",
            "dreYears",
            "bpYears",
            "fcYears",
            "currentIndicators",
            "paridade",
            "marketCap"
    })
    List<BdrEntity> findByTipoAtivo(TipoAtivo tipoAtivo);

    @EntityGraph(attributePaths = {
            "priceSeries",
            "dividendYears",
            "historicalIndicators",
            "dreYears",
            "bpYears",
            "fcYears",
            "currentIndicators",
            "paridade",
            "marketCap"
    })
    Page<BdrEntity> findByTipoAtivo(TipoAtivo tipoAtivo, Pageable pageable);

    long countByTipoAtivo(TipoAtivo tipoAtivo);
}
