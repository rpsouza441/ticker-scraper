package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;

import java.util.List;
import java.util.Optional;


public interface AcaoRepositoryPort {
    Optional<Acao> findByTicker(String ticker);
    boolean existsByTicker(String ticker);
    Acao save(Acao acao, String rawJsonAudit);
    Optional<String> findRawJsonByTicker(String ticker);

    Optional<Acao> findById(Long id);
    PagedResult<Acao> findAll(PageQuery query);
    
    // Consultas por tipo de ativo
    List<Acao> findByTipoAtivo(TipoAtivo tipoAtivo);
    PagedResult<Acao> findByTipoAtivo(TipoAtivo tipoAtivo, PageQuery query);
    long countByTipoAtivo(TipoAtivo tipoAtivo);

}
