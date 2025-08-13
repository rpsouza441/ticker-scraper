package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;

import java.util.Optional;


public interface AcaoRepositoryPort {
    Optional<Acao> findByTicker(String ticker);
    Acao save(Acao acao, String rawJsonAudit);
    Optional<String> findRawJsonByTicker(String ticker);

    Optional<Acao> findById(Long id);
    PagedResult<Acao> findAll(PageQuery query);

}
