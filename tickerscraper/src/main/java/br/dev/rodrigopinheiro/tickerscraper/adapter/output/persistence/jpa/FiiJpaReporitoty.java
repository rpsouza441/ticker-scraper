package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FiiJpaReporitoty extends JpaRepository<FundoImobiliarioEntity, Long> {
    Optional<FundoImobiliarioEntity> findByTicker(String ticker);
}
