package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.BdrEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BdrJpaRepository extends JpaRepository<BdrEntity, Long> {

    // Embora a checagem canônica seja no pai, manter esses atalhos no subtipo ajuda:
    Optional<BdrEntity> findByTicker(String ticker);
    boolean existsByTicker(String ticker);

    Page<BdrEntity> findAll(Pageable pageable);

    // “Mais recente” usando o timestamp do pai
    Optional<BdrEntity> findFirstByTickerOrderByDataAtualizacaoDesc(String ticker);

    // Carrega dividendos (coleção herdada do pai) via fetch join
    @Query("""
       select distinct b
       from BdrEntity b
       left join fetch b.dividendos d
       where b.ticker = :ticker
    """)
    Optional<BdrEntity> findByTickerWithDividendos(@Param("ticker") String ticker);

    // Útil para estratégia replace (delete-all + insert) dos dividendos do ativo
    @Modifying
    @Query("delete from br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.DividendoEntity d where d.ativo.id = :ativoId")
    void deleteAllDividendosByAtivoId(@Param("ativoId") Long ativoId);
}
