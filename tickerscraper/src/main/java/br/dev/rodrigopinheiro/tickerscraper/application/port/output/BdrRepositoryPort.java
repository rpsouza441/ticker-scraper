package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.Bdr;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;

import java.util.List;
import java.util.Optional;

public interface BdrRepositoryPort {

    Optional<Bdr> findById(Long id);

    Optional<Bdr> findByTicker(String ticker);

    Optional<Bdr> findByTickerWithDetails(String ticker);

    boolean existsByTicker(String ticker);

    PagedResult<Bdr> findAll(PageQuery query);

    Bdr saveReplacingChildren(Bdr bdr);

    List<Bdr> findByTipoAtivo(TipoAtivo tipoAtivo);

    PagedResult<Bdr> findByTipoAtivo(TipoAtivo tipoAtivo, PageQuery query);

    long countByTipoAtivo(TipoAtivo tipoAtivo);
}
