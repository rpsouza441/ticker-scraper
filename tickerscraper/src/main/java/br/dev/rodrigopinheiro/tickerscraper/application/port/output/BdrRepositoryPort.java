package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;

import java.util.Optional;

public interface BdrRepositoryPort {

    Optional<Bdr> findById(Long id);

    PagedResult<Bdr> findAll(PageQuery query);

    Optional<Bdr> findByTicker(String ticker);

    /** Retorna com dividendos carregados (fetch join). */
    Optional<Bdr> findByTickerWithDividendos(String ticker);

    boolean existsByTicker(String ticker);

    /** Upsert por ticker (cria/atualiza campos não-nulos). */
    Bdr save(Bdr bdr);
    
    /** Upsert por ticker preservando histórico de dividendos. */
    Bdr save(Bdr bdr, String rawJsonAudit);

//    /** Opcional: salva substituindo dividendos (delete-all + insert). */
//    Bdr saveReplacingDividends(Bdr bdr);

    Bdr saveReplacingDividends(Bdr bdr, String rawJsonAudit);

    Optional<String> findRawJsonByTicker(String ticker);


}
