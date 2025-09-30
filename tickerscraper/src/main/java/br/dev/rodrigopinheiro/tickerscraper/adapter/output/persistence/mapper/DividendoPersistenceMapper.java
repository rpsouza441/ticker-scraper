package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.DividendoEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Dividendo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DividendoPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true) // Será configurado pelo BdrPersistenceMapper
    DividendoEntity toEntity(Dividendo source);

    Dividendo toDomain(DividendoEntity entity);

    // Métodos de lista simplificados
    default List<DividendoEntity> toEntity(List<Dividendo> source) {
        if (source == null) {
            return new ArrayList<>();
        }
        
        List<DividendoEntity> entities = new ArrayList<>();
        for (Dividendo dividendo : source) {
            entities.add(toEntity(dividendo));
        }
        return entities;
    }

    default List<Dividendo> toDomain(List<DividendoEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        
        List<Dividendo> dividendos = new ArrayList<>();
        for (DividendoEntity entity : entities) {
            dividendos.add(toDomain(entity));
        }
        return dividendos;
    }
}
