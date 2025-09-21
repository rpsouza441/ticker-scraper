package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.FiiJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.FiiPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Component
public class FiiRepositoryAdapter implements FiiRepositoryPort {
    private static final Logger logger = LoggerFactory.getLogger(FiiRepositoryAdapter.class);

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
    public boolean existsByTicker(String ticker) {
        return jpa.existsByTicker(ticker);
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
    @Transactional
    public FundoImobiliario saveReplacingDividends(FundoImobiliario fii, Long internalId, String rawJsonAudit) {
        logger.debug("Iniciando saveReplacingDividends para ticker: {}, dividendos: {}", 
                fii.getTicker(), fii.getFiiDividendos() != null ? fii.getFiiDividendos().size() : 0);
        
        FundoImobiliarioEntity entity = jpa.findByTicker(fii.getTicker())
                .orElseGet(() -> {
                    logger.debug("Criando nova entidade para ticker: {}", fii.getTicker());
                    return mapper.toEntity(fii);
                });

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

        // Salvar a entidade principal primeiro para garantir que tenha ID
        entity = jpa.save(entity);
        jpa.flush(); // Garante que a entidade principal seja persistida
        logger.debug("Entidade principal salva com ID: {} para ticker: {}", entity.getId(), fii.getTicker());
        
        // Limpar dividendos existentes antes de adicionar novos (evita constraint violation)
        logger.debug("Removendo dividendos existentes para fundo ID: {}", entity.getId());
        jpa.deleteAllDividendosByFundoId(entity.getId());
        jpa.flush(); // Força a execução do DELETE antes do INSERT
        logger.debug("Dividendos existentes removidos para fundo ID: {}", entity.getId());
        
        // dividendos (12 meses, FK/back-ref)
        logger.debug("Adicionando {} novos dividendos para ticker: {}", 
                fii.getFiiDividendos() != null ? fii.getFiiDividendos().size() : 0, fii.getTicker());
        mapper.replaceDividendos(fii, entity);

        // Salvar novamente para persistir os dividendos
        entity = jpa.save(entity);
        logger.debug("Entidade final salva com {} dividendos para ticker: {}", 
                entity.getFiiDividendos() != null ? entity.getFiiDividendos().size() : 0, fii.getTicker());
        
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<String> findRawJsonByTicker(String ticker) {
        return jpa.findByTicker(ticker).map(FundoImobiliarioEntity::getDadosBrutosJson);
    }

    @Override
    public List<FundoImobiliario> findByTipoAtivo(TipoAtivo tipoAtivo) {
        return jpa.findByTipoAtivo(tipoAtivo).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public PagedResult<FundoImobiliario> findByTipoAtivo(TipoAtivo tipoAtivo, PageQuery query) {
        Pageable pageable = PageRequest.of(query.pageNumber(), query.pageSize());
        Page<FundoImobiliarioEntity> page = jpa.findByTipoAtivo(tipoAtivo, pageable);
        List<FundoImobiliario> content = page.getContent().stream().map(mapper::toDomain).toList();
        return new PagedResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    @Override
    public long countByTipoAtivo(TipoAtivo tipoAtivo) {
        return jpa.countByTipoAtivo(tipoAtivo);
    }
}
