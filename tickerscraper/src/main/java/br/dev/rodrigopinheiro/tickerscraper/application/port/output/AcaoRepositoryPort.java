package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;

import java.util.Optional;

public interface AcaoRepositoryPort {

    AcaoEntity save(AcaoEntity acaoEntity);
    Optional<Acao> findById(Long id);
    PagedResult<Acao> findAll(PageQuery query);

    Optional<AcaoEntity> findByTicker(String ticker);
}

