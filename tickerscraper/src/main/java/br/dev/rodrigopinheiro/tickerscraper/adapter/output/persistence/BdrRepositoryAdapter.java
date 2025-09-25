package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.BdrEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.BdrJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.BdrPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.BdrRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class BdrRepositoryAdapter implements BdrRepositoryPort {
    private static final Logger log = LoggerFactory.getLogger(BdrRepositoryAdapter.class);

    private final BdrJpaRepository jpa;
    private final BdrPersistenceMapper mapper;

    public BdrRepositoryAdapter(BdrJpaRepository jpa, BdrPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<Bdr> findByTicker(String ticker) {
        return jpa.findFirstByTickerOrderByUpdatedAtDesc(ticker).map(mapper::toDomain);
    }

    @Override
    public boolean existsByTicker(String ticker) {
        return jpa.existsByTicker(ticker);
    }

    @Override
    public Optional<Bdr> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public PagedResult<Bdr> findAll(PageQuery query) {
        Pageable pageable = PageRequest.of(query.pageNumber(), query.pageSize(), Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<BdrEntity> page = jpa.findAll(pageable);
        List<Bdr> content = page.getContent().stream().map(mapper::toDomain).toList();
        return new PagedResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    @Override
    @Transactional
    public Bdr save(Bdr bdr) {
        // upsert por ticker
        BdrEntity entity = jpa.findFirstByTickerOrderByUpdatedAtDesc(bdr.getTicker())
                .orElseGet(() -> mapper.toEntity(bdr));

        if (entity.getId() != null) {
            // Atualiza apenas campos não-nulos vindos do domínio
            mapper.updateEntity(bdr, entity);
        }

        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }
}
