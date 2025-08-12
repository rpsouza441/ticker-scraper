package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.FiiJpaReporitoty;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.FiiPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FiiRepositoryAdapter implements FiiRepositoryPort {

    private final FiiJpaReporitoty repository;
    private final FiiPersistenceMapper mapper;

    public FiiRepositoryAdapter(FiiJpaReporitoty repository, FiiPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<FundoImobiliarioEntity> findByTicker(String ticker) {
        return repository.findByTicker(ticker);
    }

    @Override
    public FundoImobiliarioEntity save(FundoImobiliarioEntity entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<FundoImobiliario> findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public PagedResult<FundoImobiliario> findAll(PageQuery query) {
        Pageable pageable = PageRequest.of(query.pageNumber(), query.pageSize());
        Page<FundoImobiliarioEntity> pageOfEntities = repository.findAll(pageable);

        List<FundoImobiliario> fiiDaPagina = pageOfEntities.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new PagedResult<>(
                fiiDaPagina,
                pageOfEntities.getTotalElements(),
                pageOfEntities.getTotalPages(),
                pageOfEntities.getNumber()
        );
    }
}
