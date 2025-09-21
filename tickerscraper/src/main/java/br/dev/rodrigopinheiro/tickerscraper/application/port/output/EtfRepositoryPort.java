package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Etf;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;

import java.util.List;
import java.util.Optional;

public interface EtfRepositoryPort {
    Optional<Etf> findByTicker(String ticker);
    boolean existsByTicker(String ticker);
    Etf save(Etf etf, String rawJsonAudit);
    Optional<String> findRawJsonByTicker(String ticker);

    Optional<Etf> findById(Long id);
    PagedResult<Etf> findAll(PageQuery query);
    
    // Consultas por tipo de ativo
    List<Etf> findByTipoAtivo(TipoAtivo tipoAtivo);
    PagedResult<Etf> findByTipoAtivo(TipoAtivo tipoAtivo, PageQuery query);
    long countByTipoAtivo(TipoAtivo tipoAtivo);
}