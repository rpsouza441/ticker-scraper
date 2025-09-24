package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr.BdrMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.BdrRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.Bdr;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Component
public class BdrRepositoryAdapter implements BdrRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(BdrRepositoryAdapter.class);

    private final BdrJpaRepository repository;
    private final BdrMapper mapper;
    public BdrRepositoryAdapter(BdrJpaRepository repository, BdrMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bdr> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bdr> findByTicker(String ticker) {
        return repository.findByTicker(ticker).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bdr> findByTickerWithDetails(String ticker) {
        return repository.findDetailedByTicker(ticker).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTicker(String ticker) {
        return repository.existsByTicker(ticker);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Bdr> findAll(PageQuery query) {
        Pageable pageable = PageRequest.of(query.pageNumber(), query.pageSize());
        Page<BdrEntity> page = repository.findAll(pageable);
        List<Bdr> content = page.getContent().stream().map(mapper::toDomain).toList();
        return new PagedResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    @Override
    @Transactional
    public Bdr saveReplacingChildren(Bdr bdr) {
        logger.debug("Persistindo BDR {} com atualização de coleções", bdr.getTicker());
        BdrEntity entity = repository.findDetailedByTicker(bdr.getTicker())
                .orElseGet(() -> mapper.toEntity(bdr));

        if (entity.getId() != null) {
            mapper.updateEntity(bdr, entity);
        }

        mapper.replaceAssociations(bdr, entity);
        entity.setUpdatedAt(bdr.getUpdatedAt() == null ? null : bdr.getUpdatedAt().atOffset(ZoneOffset.UTC));

        entity = repository.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bdr> findByTipoAtivo(TipoAtivo tipoAtivo) {
        return repository.findByTipoAtivo(tipoAtivo).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Bdr> findByTipoAtivo(TipoAtivo tipoAtivo, PageQuery query) {
        Pageable pageable = PageRequest.of(query.pageNumber(), query.pageSize());
        Page<BdrEntity> page = repository.findByTipoAtivo(tipoAtivo, pageable);
        List<Bdr> content = page.getContent().stream().map(mapper::toDomain).toList();
        return new PagedResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTipoAtivo(TipoAtivo tipoAtivo) {
        return repository.countByTipoAtivo(tipoAtivo);
    }
}
