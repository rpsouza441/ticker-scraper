package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.BdrEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.AtivoFinanceiroJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.BdrJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.BdrPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.DividendoPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PageQuery;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.PagedResult;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.BdrRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BdrRepositoryAdapter implements BdrRepositoryPort {

    private final BdrJpaRepository bdrJpa;
    private final AtivoFinanceiroJpaRepository ativoJpa;
    private final BdrPersistenceMapper mapper;
    private final DividendoPersistenceMapper dividendoMapper;

    private static String norm(String t) {
        return t == null ? null : t.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public Optional<Bdr> findById(Long id) {
        return bdrJpa.findById(id).map(e -> mapper.toDomain(e, dividendoMapper));
    }

    @Override
    public PagedResult<Bdr> findAll(PageQuery query) {
        var page = bdrJpa.findAll(PageRequest.of(query.pageNumber(), query.pageSize()));
        List<Bdr> content = page.getContent().stream()
                .map(e -> mapper.toDomain(e, dividendoMapper))
                .collect(Collectors.toCollection(ArrayList::new));
        return new PagedResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    @Override
    public Optional<Bdr> findByTicker(String ticker) {
        String t = norm(ticker);
        return bdrJpa.findByTicker(t).map(e -> mapper.toDomain(e, dividendoMapper));
    }

    @Override
    public Optional<Bdr> findByTickerWithDividendos(String ticker) {
        String t = norm(ticker);
        return bdrJpa.findByTickerWithDividendos(t).map(e -> mapper.toDomain(e, dividendoMapper));
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
        System.out.println("=== DEBUG: BdrRepositoryAdapter.save CHAMADO! Ticker: " + bdr.getTicker() + " ===");
        // upsert por ticker
        String t = norm(bdr.getTicker());
        bdr.setTicker(t);

        BdrEntity entity = bdrJpa.findByTicker(t)
                .orElseGet(() -> mapper.toEntity(bdr, dividendoMapper));

        if (entity.getId() != null) {
            mapper.updateEntity(bdr, entity); // merge ignorando nulos
        }

        entity = bdrJpa.save(entity);
        return mapper.toDomain(entity, dividendoMapper);
    }


    @Override
    @Transactional
    public Bdr saveReplacingDividends(Bdr bdr, String rawJsonAudit) {
        String t = norm(bdr.getTicker());
        log.info("BdrRepositoryAdapter.saveReplacingDividends - Iniciando para ticker: {}", t);

        // Buscar entidade existente ou criar nova
        BdrEntity entity = bdrJpa.findByTicker(t).orElse(null);
        boolean isUpdate = (entity != null);

        if (isUpdate) {
            log.info("BdrRepositoryAdapter.saveReplacingDividends - Atualizando BDR existente com ID: {}", entity.getId());
            // Atualizar dados básicos (sem dividendos)
            mapper.updateEntity(bdr, entity);
            
            // Atualizar dividendos usando o método simplificado do mapper
            mapper.updateDividendos(bdr, entity, dividendoMapper);
        } else {
            log.info("BdrRepositoryAdapter.saveReplacingDividends - Criando novo BDR");
            entity = mapper.toEntity(bdr, dividendoMapper);
            
            // Configurar relacionamento bidirecional para dividendos
            if (entity.getDividendos() != null) {
                for (var dividendo : entity.getDividendos()) {
                    dividendo.setAtivo(entity);
                }
            }
        }

        // Definir dados brutos JSON se fornecido
        if (rawJsonAudit != null) {
            log.info("BdrRepositoryAdapter.saveReplacingDividends - Definindo dados brutos JSON");
            entity.setDadosBrutosJson(rawJsonAudit);
        }

        // Salvar entidade (cascade irá gerenciar os dividendos automaticamente)
        log.info("BdrRepositoryAdapter.saveReplacingDividends - Salvando entidade");
        entity = bdrJpa.save(entity);
        log.info("BdrRepositoryAdapter.saveReplacingDividends - Entidade salva com ID: {}", entity.getId());

        // Converter para domínio e retornar
        log.info("BdrRepositoryAdapter.saveReplacingDividends - Convertendo para domínio");
        Bdr result = mapper.toDomain(entity, dividendoMapper);
        log.info("BdrRepositoryAdapter.saveReplacingDividends - Processo concluído para ticker: {}", result.getTicker());
        
        return result;
    }



    @Override
    public Optional<String> findRawJsonByTicker(String ticker) {
        String t = norm(ticker);
        return bdrJpa.findByTicker(t).map(BdrEntity::getDadosBrutosJson);
    }
}
