package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;

import java.util.Optional;

public interface FiiRepositoryPort {

    FundoImobiliarioEntity save(FundoImobiliarioEntity acaoEntity);
    Optional<FundoImobiliario> findById(Long id);
    PagedResult<FundoImobiliario> findAll(PageQuery query);

    Optional<FundoImobiliarioEntity> findByTicker(String ticker);
}

