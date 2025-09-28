package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.BdrEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.AtivoFinanceiroJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.BdrJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.BdrPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.BdrRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class BdrRepositoryAdapter implements BdrRepositoryPort {

    private final BdrJpaRepository bdrJpa;
    private final AtivoFinanceiroJpaRepository ativoJpa;
    private final BdrPersistenceMapper mapper;

    public BdrRepositoryAdapter(BdrJpaRepository bdrJpa,
                                AtivoFinanceiroJpaRepository ativoJpa,
                                BdrPersistenceMapper mapper) {
        this.bdrJpa = bdrJpa;
        this.ativoJpa = ativoJpa;
        this.mapper = mapper;
    }

    private static String norm(String t) {
        return t == null ? null : t.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public Optional<Bdr> findById(Long id) {
        return bdrJpa.findById(id).map(e -> mapper.toDomain(e, null));
    }

    @Override
    public PagedResult<Bdr> findAll(PageQuery query) {
        var page = bdrJpa.findAll(PageRequest.of(query.pageNumber(), query.pageSize()));
        List<Bdr> content = page.getContent().stream()
                .map(e -> mapper.toDomain(e, null))
                .toList();
        return new PagedResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    @Override
    public Optional<Bdr> findByTicker(String ticker) {
        String t = norm(ticker);
        return bdrJpa.findByTicker(t).map(e -> mapper.toDomain(e, null));
    }

    @Override
    public Optional<Bdr> findByTickerWithDividendos(String ticker) {
        String t = norm(ticker);
        return bdrJpa.findByTickerWithDividendos(t).map(e -> mapper.toDomain(e, null));
    }

    @Override
    public boolean existsByTicker(String ticker) {
        // canônico: checar no pai (tabela ATIVO) para garantir unicidade global do ticker
        String t = norm(ticker);
        return ativoJpa.existsByTicker(t);
    }

    @Override
    @Transactional
    public Bdr save(Bdr bdr) {
        // upsert por ticker
        String t = norm(bdr.getTicker());
        bdr.setTicker(t);

        BdrEntity entity = bdrJpa.findByTicker(t)
                .orElseGet(() -> mapper.toEntity(bdr, null));

        if (entity.getId() != null) {
            mapper.updateEntity(bdr, entity); // merge ignorando nulos
        }

        entity = bdrJpa.save(entity);
        return mapper.toDomain(entity, null);
    }

    @Override
    @Transactional
    public Bdr saveReplacingDividends(Bdr bdr) {
        String t = norm(bdr.getTicker());
        bdr.setTicker(t);

        // carrega com dividendos (para evitar N+1 e garantir replace correto)
        BdrEntity entity = bdrJpa.findByTickerWithDividendos(t)
                .orElseGet(() -> mapper.toEntity(bdr, null));

        if (entity.getId() != null) {
            mapper.updateEntity(bdr, entity);
        }

        // garante ID antes de mexer na coleção
        entity = bdrJpa.save(entity);

        // replace da coleção (orphanRemoval = true em AtivoFinanceiroEntity)
        mapper.replaceDividendos(bdr, entity, null);

        entity = bdrJpa.save(entity);
        return mapper.toDomain(entity, null);
    }
}
