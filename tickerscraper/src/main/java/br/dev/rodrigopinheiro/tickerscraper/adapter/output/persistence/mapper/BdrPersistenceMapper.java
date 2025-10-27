package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.BdrEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.DividendoEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Dividendo;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = { DividendoPersistenceMapper.class },
        config = AtivoFinanceiroMappingConfig.class
)
public interface BdrPersistenceMapper {

    // Domain -> Entity (create)
    @InheritConfiguration(name = "mapCommonToEntity")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dividendos", source = "dividendos", qualifiedByName = "mapDividendosToEntity")
    @Mapping(target = "dadosBrutosJson", ignore = true)
    BdrEntity toEntity(Bdr source, @Context DividendoPersistenceMapper divMapper);

    @AfterMapping
    default void afterToEntity(Bdr source, @MappingTarget BdrEntity target, @Context DividendoPersistenceMapper divMapper) {
        // Configurações básicas da entidade
        target.setTipoAtivo(TipoAtivo.BDR);
        if (target.getTicker() != null) {
            target.setTicker(target.getTicker().trim().toUpperCase());
        }
    }

    // Entity -> Domain
    @InheritConfiguration(name = "mapCommonToDomain")
    @Mapping(target = "dividendos", ignore = true) // Tratado no @AfterMapping
    Bdr toDomain(BdrEntity entity, @Context DividendoPersistenceMapper divMapper);

    @AfterMapping
    default void afterToDomain(BdrEntity source, @MappingTarget Bdr target, @Context DividendoPersistenceMapper divMapper) {
        // Mapear dividendos da entidade para o domínio
        List<Dividendo> dividendos = mapDividendosToDomain(source.getDividendos(), divMapper);
        target.replaceDividendos(dividendos);
        target.setTipoAtivo(TipoAtivo.BDR);
    }

    // Update parcial (sem dividendos - eles são gerenciados pelo cascade)
    @InheritConfiguration(name = "updateCommonEntity")
    @Mapping(target = "dividendos", ignore = true)
    @Mapping(target = "dadosBrutosJson", ignore = true)
    void updateEntity(Bdr source, @MappingTarget BdrEntity target);

    // Métodos auxiliares para mapeamento de dividendos
    @Named("mapDividendosToEntity")
    default List<DividendoEntity> mapDividendosToEntity(List<Dividendo> dividendos, @Context DividendoPersistenceMapper divMapper) {
        if (dividendos == null || dividendos.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<DividendoEntity> entities = new ArrayList<>();
        for (Dividendo dividendo : dividendos) {
            DividendoEntity entity = divMapper.toEntity(dividendo);
            entities.add(entity);
        }
        
        // Ordenar por mês (mais recente primeiro)
        entities.sort(Comparator.comparing(DividendoEntity::getMes).reversed());
        return entities;
    }

    default List<Dividendo> mapDividendosToDomain(List<DividendoEntity> entities, DividendoPersistenceMapper divMapper) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Dividendo> dividendos = divMapper.toDomain(entities);
        // Ordenar por mês (mais recente primeiro)
        dividendos.sort(Comparator.comparing(Dividendo::getMes).reversed());
        return dividendos;
    }

    // Método para atualizar dividendos com lógica de upsert
    default void updateDividendos(Bdr source, @MappingTarget BdrEntity target, @Context DividendoPersistenceMapper divMapper) {
        if (source.getDividendos() == null || source.getDividendos().isEmpty()) {
            return;
        }


        // Para cada dividendo do domínio, verificar se já existe na entidade
        for (Dividendo dividendoDomain : source.getDividendos()) {
            String mesDomain = dividendoDomain.getMes().toString();


            // Buscar dividendo existente pela chave composta
            DividendoEntity dividendoExistente = target.getDividendos().stream()
                .filter(d -> {
                    boolean mesIgual = d.getMes().equals(mesDomain);
                    boolean tipoIgual = d.getTipoDividendo().equals(dividendoDomain.getTipoDividendo());
                    boolean moedaIgual = d.getMoeda().equals(dividendoDomain.getMoeda());

                    
                    return mesIgual && tipoIgual && moedaIgual;
                })
                .findFirst()
                .orElse(null);

            if (dividendoExistente != null) {
                // Atualizar dividendo existente
                dividendoExistente.setValor(dividendoDomain.getValor());
            } else {
                // Criar novo dividendo
                DividendoEntity novoDividendo = divMapper.toEntity(dividendoDomain);
                novoDividendo.setAtivo(target);
                target.getDividendos().add(novoDividendo);
            }
        }
    }
}
