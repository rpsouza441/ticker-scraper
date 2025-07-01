package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AcaoJpaReporitoty extends JpaRepository<AcaoEntity, Long> {
    Optional<AcaoEntity> findByTicker(String ticker);

}
