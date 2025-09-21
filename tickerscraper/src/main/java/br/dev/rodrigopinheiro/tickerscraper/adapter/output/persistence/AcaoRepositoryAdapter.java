package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.AcaoJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.AcaoPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AcaoRepositoryAdapter implements AcaoRepositoryPort {

    private final AcaoJpaRepository jpa;
    private final AcaoPersistenceMapper mapper;

    public AcaoRepositoryAdapter(AcaoJpaRepository jpa, AcaoPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<Acao> findByTicker(String ticker) {
        return jpa.findByTicker(ticker).map(mapper::toDomain);
    }

    @Override
    public boolean existsByTicker(String ticker) {
        return jpa.existsByTicker(ticker);
    }

    @Override
    public Acao save(Acao acao, String rawJsonAudit) {
        // upsert por ticker
        AcaoEntity entity = jpa.findByTicker(acao.getTicker())
                .orElseGet(() -> mapper.toEntity(acao));

        if (entity.getId() != null) {
            mapper.updateEntity(acao, entity); // seu mapper deve ter @BeanMapping IGNORE nulos
        }
        if (rawJsonAudit != null) {
            entity.setDadosBrutosJson(rawJsonAudit);
        }

        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<String> findRawJsonByTicker(String ticker) {
        return jpa.findByTicker(ticker).map(AcaoEntity::getDadosBrutosJson);
    }

    @Override
    public Optional<Acao> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public PagedResult<Acao> findAll(PageQuery query) {
        Pageable pageable = PageRequest.of(query.pageNumber(), query.pageSize());
        Page<AcaoEntity> page = jpa.findAll(pageable);
        List<Acao> content = page.getContent().stream().map(mapper::toDomain).toList();
        return new PagedResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    @Override
    public List<Acao> findByTipoAtivo(TipoAtivo tipoAtivo) {
        return jpa.findByTipoAtivo(tipoAtivo).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public PagedResult<Acao> findByTipoAtivo(TipoAtivo tipoAtivo, PageQuery query) {
        Pageable pageable = PageRequest.of(query.pageNumber(), query.pageSize());
        Page<AcaoEntity> page = jpa.findByTipoAtivo(tipoAtivo, pageable);
        List<Acao> content = page.getContent().stream().map(mapper::toDomain).toList();
        return new PagedResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    @Override
    public long countByTipoAtivo(TipoAtivo tipoAtivo) {
        return jpa.countByTipoAtivo(tipoAtivo);
    }
}