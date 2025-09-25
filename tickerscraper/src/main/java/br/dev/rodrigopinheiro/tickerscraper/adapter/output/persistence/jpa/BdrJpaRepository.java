package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.BdrEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BdrJpaRepository extends JpaRepository<BdrEntity, Long> {

    Optional<BdrEntity> findFirstByTickerOrderByUpdatedAtDesc(String ticker);

    boolean existsByTicker(String ticker);

    Page<BdrEntity> findAll(Pageable pageable);
}
