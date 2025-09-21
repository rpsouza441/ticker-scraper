package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.EtfEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtfJpaRepository extends JpaRepository<EtfEntity, Long> {
    Optional<EtfEntity> findByTicker(String ticker);
    boolean existsByTicker(String ticker);
    
    // Consultas por tipo de ativo
    List<EtfEntity> findByTipoAtivo(TipoAtivo tipoAtivo);
    Page<EtfEntity> findByTipoAtivo(TipoAtivo tipoAtivo, Pageable pageable);
    long countByTipoAtivo(TipoAtivo tipoAtivo);
}