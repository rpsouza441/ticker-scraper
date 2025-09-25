package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.EtfEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.EtfJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.EtfEntityMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.EtfRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Etf;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EtfRepositoryAdapter implements EtfRepositoryPort {

    private final EtfJpaRepository jpa;
    private final EtfEntityMapper mapper;

    public EtfRepositoryAdapter(EtfJpaRepository jpa, EtfEntityMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<Etf> findByTicker(String ticker) {
        return jpa.findByTicker(ticker).map(mapper::toDomain);
    }

    @Override
    public boolean existsByTicker(String ticker) {
        return jpa.existsByTicker(ticker);
    }
    @Override
    public Etf save(Etf etf, String rawJsonAudit) {
        // upsert por ticker
        EtfEntity entity = jpa.findByTicker(etf.getTicker())
                .orElseGet(() -> mapper.toEntity(etf));

        if (entity.getId() != null) {
            mapper.updateEntity(entity, etf); // seu mapper deve ter @BeanMapping IGNORE nulos
        }
        if (rawJsonAudit != null) {
            entity.setDadosBrutosJson(rawJsonAudit);
        }

        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<String> findRawJsonByTicker(String ticker) {
        return jpa.findByTicker(ticker).map(EtfEntity::getDadosBrutosJson);
    }

    @Override
    public Optional<Etf> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public PagedResult<Etf> findAll(PageQuery query) {
        Pageable pageable = PageRequest.of(query.pageNumber(), query.pageSize());
        Page<EtfEntity> page = jpa.findAll(pageable);
        List<Etf> content = page.getContent().stream().map(mapper::toDomain).toList();
        return new PagedResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    @Override
    public List<Etf> findByTipoAtivo(TipoAtivo tipoAtivo) {
        return jpa.findByTipoAtivo(tipoAtivo).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public PagedResult<Etf> findByTipoAtivo(TipoAtivo tipoAtivo, PageQuery query) {
        Pageable pageable = PageRequest.of(query.pageNumber(), query.pageSize());
        Page<EtfEntity> page = jpa.findByTipoAtivo(tipoAtivo, pageable);
        List<Etf> content = page.getContent().stream().map(mapper::toDomain).toList();
        return new PagedResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    @Override
    public long countByTipoAtivo(TipoAtivo tipoAtivo) {
        return jpa.countByTipoAtivo(tipoAtivo);
    }
}