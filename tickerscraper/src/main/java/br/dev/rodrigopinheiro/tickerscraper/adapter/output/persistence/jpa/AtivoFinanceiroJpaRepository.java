package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AtivoFinanceiroEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface AtivoFinanceiroJpaRepository extends JpaRepository<AtivoFinanceiroEntity, Long> {

    // Identidade canônica por ticker (na tabela pai)
    boolean existsByTicker(String ticker);

    Optional<AtivoFinanceiroEntity> findByTicker(String ticker);

    @Query("select a.id from AtivoFinanceiroEntity a where a.ticker = :ticker")
    Optional<Long> findIdByTicker(@Param("ticker") String ticker);

    // “Mais recente” pelo timestamp da raiz
    Optional<AtivoFinanceiroEntity> findFirstByTickerOrderByDataAtualizacaoDesc(String ticker);

    // Carrega dividendos (coleção do pai) com EntityGraph
    @EntityGraph(attributePaths = "dividendos")
    Optional<AtivoFinanceiroEntity> findWithDividendosByTicker(String ticker);

    // Filtros gerais por tipo (útil para listagens)
    Page<AtivoFinanceiroEntity> findByTipoAtivo(TipoAtivo tipoAtivo, Pageable pageable);
    long countByTipoAtivo(TipoAtivo tipoAtivo);

    // Lock para cenários de upsert concorrente
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AtivoFinanceiroEntity a where a.ticker = :ticker")
    Optional<AtivoFinanceiroEntity> lockByTicker(@Param("ticker") String ticker);
}
