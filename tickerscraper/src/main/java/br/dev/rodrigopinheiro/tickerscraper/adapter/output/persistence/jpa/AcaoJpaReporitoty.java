package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AcaoJpaReporitoty extends JpaRepository<AcaoEntity, Long> {

}
