package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.AcaoJpaReporitoty;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.AcaoPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AcaoRepositoryAdapter implements AcaoRepositoryPort {
    private final AcaoJpaReporitoty repository;
    private final AcaoPersistenceMapper mapper;

    public AcaoRepositoryAdapter(AcaoJpaReporitoty repository, AcaoPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Acao save(Acao acao) {
        AcaoEntity entity = mapper.toEntity(acao);
        AcaoEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Acao> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PagedResult<Acao> findAll(PageQuery query) {
        Pageable pageable = PageRequest.of(query.pageNumber(), query.pageSize());
        Page<AcaoEntity> pageOfEntities = repository.findAll(pageable);

        List<Acao> acoesDaPagina = pageOfEntities.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new PagedResult<>(
                acoesDaPagina,
                pageOfEntities.getTotalElements(),
                pageOfEntities.getTotalPages(),
                pageOfEntities.getNumber()
        );
    }
}
