package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;

import java.util.List;
import java.util.Optional;

public interface FiiRepositoryPort {


    Optional<FundoImobiliario> findById(Long id);

    PagedResult<FundoImobiliario> findAll(PageQuery query);

    Optional<FundoImobiliario> findByTicker(String ticker);

    boolean existsByTicker(String ticker);

    Optional<FundoImobiliario> findByTickerWithDividendos(String ticker);

    FundoImobiliario saveReplacingDividends(FundoImobiliario fii, Long internalId, String rawJsonAudit);

    Optional<String> findRawJsonByTicker(String ticker);
    
    // Consultas por tipo de ativo
    List<FundoImobiliario> findByTipoAtivo(TipoAtivo tipoAtivo);
    PagedResult<FundoImobiliario> findByTipoAtivo(TipoAtivo tipoAtivo, PageQuery query);
    long countByTipoAtivo(TipoAtivo tipoAtivo);

}

