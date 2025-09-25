package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;

import java.util.Optional;

public interface BdrRepositoryPort {

    Optional<Bdr> findById(Long id);

    PagedResult<Bdr> findAll(PageQuery query);

    Optional<Bdr> findByTicker(String ticker);

    boolean existsByTicker(String ticker);

    /** Upsert por ticker: cria se não existir; atualiza campos não-nulos se existir. */
    Bdr save(Bdr bdr);
}
