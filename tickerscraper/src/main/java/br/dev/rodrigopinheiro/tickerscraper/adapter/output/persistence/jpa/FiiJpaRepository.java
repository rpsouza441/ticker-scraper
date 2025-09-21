package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FiiJpaRepository extends JpaRepository<FundoImobiliarioEntity, Long> {
    Optional<FundoImobiliarioEntity> findByTicker(String ticker);
    boolean existsByTicker(String ticker);

    @Query("""
       select distinct f
       from FundoImobiliarioEntity f
       left join fetch f.fiiDividendos d
       where f.ticker = :ticker
    """)
    Optional<FundoImobiliarioEntity> findByTickerWithDividendos(@Param("ticker") String ticker);

    @Modifying
    @Query("DELETE FROM FiiDividendoEntity d WHERE d.fundoImobiliario.id = :fundoId")
    void deleteAllDividendosByFundoId(@Param("fundoId") Long fundoId);

    // Consultas por tipo de ativo
    List<FundoImobiliarioEntity> findByTipoAtivo(TipoAtivo tipoAtivo);
    Page<FundoImobiliarioEntity> findByTipoAtivo(TipoAtivo tipoAtivo, Pageable pageable);
    long countByTipoAtivo(TipoAtivo tipoAtivo);

}
