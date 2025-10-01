package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.BdrEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.DividendoEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.AtivoFinanceiroJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.BdrJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.DividendoJpaRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BdrRepositoryAdapter implements BdrRepositoryPort {

    private final BdrJpaRepository bdrJpa;
    private final AtivoFinanceiroJpaRepository ativoJpa;
    private final DividendoJpaRepository dividendoJpa;
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
        log.info("BdrRepositoryAdapter.save - Delegando para savePreservingDividendHistory para ticker: {}", bdr.getTicker());
        return savePreservingDividendHistory(bdr, null);
    }

    @Override
    @Transactional
    public Bdr save(Bdr bdr, String rawJsonAudit) {
        log.info("BdrRepositoryAdapter.save(com audit) - Delegando para savePreservingDividendHistory para ticker: {}", bdr.getTicker());
        return savePreservingDividendHistory(bdr, rawJsonAudit);
    }


    @Override
    @Transactional
    public Bdr saveReplacingDividends(Bdr bdr, String rawJsonAudit) {
        String t = norm(bdr.getTicker());
        log.info("BdrRepositoryAdapter.saveReplacingDividends - Iniciando para ticker: {}", t);

        // Buscar entidade existente ou criar nova
        BdrEntity entity = bdrJpa.findByTicker(t).orElse(null);
        boolean isUpdate = (entity != null);

        
        // Se é uma atualização, carregar os dividendos explicitamente (lazy loading)
        if (isUpdate && entity != null) {
            // Força o carregamento dos dividendos lazy
            int dividendosCount = entity.getDividendos().size();
        }

        if (isUpdate) {
            log.info("BdrRepositoryAdapter.saveReplacingDividends - Atualizando BDR existente com ID: {}", entity.getId());
            // Atualizar dados básicos (sem dividendos)
            mapper.updateEntity(bdr, entity);
            
            // Estratégia mais robusta: deletar explicitamente os dividendos existentes
            dividendoJpa.deleteByAtivoId(entity.getId());
            
            // Recarregar a entidade para sincronizar com o banco após o delete
            entity = bdrJpa.findById(entity.getId()).orElseThrow();
            
            // Adicionar novos dividendos
            if (bdr.getDividendos() != null && !bdr.getDividendos().isEmpty()) {
                for (var dividendoDomain : bdr.getDividendos()) {
                    DividendoEntity dividendoEntity = dividendoMapper.toEntity(dividendoDomain);
                    dividendoEntity.setAtivo(entity);
                    entity.getDividendos().add(dividendoEntity);
                }
            }
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

    /**
     * Salva BDR preservando histórico de dividendos.
     * Ao invés de deletar todos os dividendos, faz merge inteligente:
     * - Mantém dividendos antigos que não estão nos novos dados
     * - Atualiza dividendos existentes se houver mudanças
     * - Adiciona novos dividendos
     */
    @Transactional
    public Bdr savePreservingDividendHistory(Bdr bdr, String rawJsonAudit) {
        String t = norm(bdr.getTicker());
        log.info("BdrRepositoryAdapter.savePreservingDividendHistory - Iniciando para ticker: {}", t);

        // Buscar entidade existente ou criar nova
        BdrEntity entity = bdrJpa.findByTicker(t).orElse(null);
        boolean isUpdate = (entity != null);

        if (isUpdate) {
            log.info("BdrRepositoryAdapter.savePreservingDividendHistory - Atualizando BDR existente com ID: {}", entity.getId());
            
            // Atualizar dados básicos (sem dividendos)
            mapper.updateEntity(bdr, entity);
            
            // Buscar dividendos existentes no banco
            List<DividendoEntity> dividendosExistentes = dividendoJpa.findByAtivoId(entity.getId());
            log.info("BdrRepositoryAdapter.savePreservingDividendHistory - Encontrados {} dividendos existentes", dividendosExistentes.size());
            
            // Criar mapa dos dividendos existentes para busca rápida
            Map<String, DividendoEntity> dividendosExistentesMap = dividendosExistentes.stream()
                .collect(Collectors.toMap(
                    d -> d.getMes() + "|" + d.getTipoDividendo() + "|" + d.getMoeda(),
                    d -> d
                ));
            
            // Processar novos dividendos
            Set<String> chavesProcessadas = new HashSet<>();
            if (bdr.getDividendos() != null && !bdr.getDividendos().isEmpty()) {
                for (var dividendoDomain : bdr.getDividendos()) {
                    String chave = dividendoDomain.getMes().toString() + "|" + 
                                 dividendoDomain.getTipoDividendo() + "|" + 
                                 dividendoDomain.getMoeda();
                    
                    chavesProcessadas.add(chave);
                    
                    DividendoEntity dividendoExistente = dividendosExistentesMap.get(chave);
                    
                    if (dividendoExistente != null) {
                        // Atualizar dividendo existente se valor mudou
                        if (!dividendoExistente.getValor().equals(dividendoDomain.getValor())) {
                            log.info("BdrRepositoryAdapter.savePreservingDividendHistory - Atualizando dividendo {}: {} -> {}", 
                                   chave, dividendoExistente.getValor(), dividendoDomain.getValor());
                            dividendoExistente.setValor(dividendoDomain.getValor());
                        }
                    } else {
                        // Adicionar novo dividendo
                        log.info("BdrRepositoryAdapter.savePreservingDividendHistory - Adicionando novo dividendo: {}", chave);
                        DividendoEntity novoDividendo = dividendoMapper.toEntity(dividendoDomain);
                        novoDividendo.setAtivo(entity);
                        entity.getDividendos().add(novoDividendo);
                    }
                }
            }
            
            // Log dos dividendos históricos que foram preservados
            long dividendosPreservados = dividendosExistentesMap.keySet().stream()
                .filter(chave -> !chavesProcessadas.contains(chave))
                .count();
            
            if (dividendosPreservados > 0) {
                log.info("BdrRepositoryAdapter.savePreservingDividendHistory - Preservados {} dividendos históricos", dividendosPreservados);
            }
            
        } else {
            log.info("BdrRepositoryAdapter.savePreservingDividendHistory - Criando novo BDR");
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
            log.info("BdrRepositoryAdapter.savePreservingDividendHistory - Definindo dados brutos JSON");
            entity.setDadosBrutosJson(rawJsonAudit);
        }

        // Salvar entidade (cascade irá gerenciar os dividendos automaticamente)
        log.info("BdrRepositoryAdapter.savePreservingDividendHistory - Salvando entidade");
        entity = bdrJpa.save(entity);
        log.info("BdrRepositoryAdapter.savePreservingDividendHistory - Entidade salva com ID: {}", entity.getId());

        // Converter para domínio e retornar
        log.info("BdrRepositoryAdapter.savePreservingDividendHistory - Convertendo para domínio");
        Bdr result = mapper.toDomain(entity, dividendoMapper);
        log.info("BdrRepositoryAdapter.savePreservingDividendHistory - Processo concluído para ticker: {}", result.getTicker());
        
        return result;
    }

    @Override
    public Optional<String> findRawJsonByTicker(String ticker) {
        String t = norm(ticker);
        return bdrJpa.findByTicker(t).map(BdrEntity::getDadosBrutosJson);
    }
}
