package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.FiiJpaRepository;
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

    private final FiiJpaRepository jpa;
    private final FiiPersistenceMapper mapper;

    public FiiRepositoryAdapter(FiiJpaRepository jpa, FiiPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<FundoImobiliario> findByTicker(String ticker) {
        return jpa.findByTicker(ticker).map(mapper::toDomain);
    }

    @Override
    public Optional<FundoImobiliario> findByTickerWithDividendos(String ticker) {
        return jpa.findByTickerWithDividendos(ticker).map(mapper::toDomain);
    }

    @Override
    public Optional<FundoImobiliario> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public PagedResult<FundoImobiliario> findAll(PageQuery query) {
        Pageable pageable = PageRequest.of(query.pageNumber(), query.pageSize());
        Page<FundoImobiliarioEntity> page = jpa.findAll(pageable);
        List<FundoImobiliario> content = page.getContent().stream()
                .map(mapper::toDomain)
                .toList();
        return new PagedResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    @Override
    public FundoImobiliario saveReplacingDividends(FundoImobiliario fii, Long internalId, String rawJsonAudit) {
        FundoImobiliarioEntity entity = jpa.findByTicker(fii.getTicker())
                .orElseGet(() -> mapper.toEntity(fii));

        // UPDATE escalar (ignora nulos)
        if (entity.getId() != null) {
            mapper.updateEntity(fii, entity);
        }

        // internalId é NOT NULL + UNIQUE -> garanta preenchido
        if (entity.getId() == null) {
            entity.setInternalId(internalId);
        } else if (entity.getInternalId() == null) {
            entity.setInternalId(internalId);
        } else if (!entity.getInternalId().equals(internalId)) {
            // políticas: ou mantém, ou sincroniza — aqui vou manter e logar
            // log.warn("internalId divergente para {}. Banco={}, RAW={}", fii.getTicker(), entity.getInternalId(), internalId);
        }

        // JSONB de auditoria
        if (rawJsonAudit != null) {
            entity.setDadosBrutosJson(rawJsonAudit);
        }

        // dividendos (12 meses, FK/back-ref)
        mapper.replaceDividendos(fii, entity);

        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<String> findRawJsonByTicker(String ticker) {
        return jpa.findByTicker(ticker).map(FundoImobiliarioEntity::getDadosBrutosJson);
    }
}
