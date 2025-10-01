package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.DividendoEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoDividendo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface DividendoJpaRepository extends JpaRepository<DividendoEntity, Long> {

    /**
     * Busca um dividendo pela chave composta única
     */
    @Query("SELECT d FROM DividendoEntity d WHERE d.ativo.id = :ativoId AND d.mes = :mes AND d.tipoDividendo = :tipoDividendo AND d.moeda = :moeda")
    Optional<DividendoEntity> findByChaveComposta(
            @Param("ativoId") Long ativoId,
            @Param("mes") String mes,
            @Param("tipoDividendo") TipoDividendo tipoDividendo,
            @Param("moeda") String moeda
    );

    /**
     * Deleta todos os dividendos de um ativo específico
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DividendoEntity d WHERE d.ativo.id = :ativoId")
    void deleteByAtivoId(@Param("ativoId") Long ativoId);
}