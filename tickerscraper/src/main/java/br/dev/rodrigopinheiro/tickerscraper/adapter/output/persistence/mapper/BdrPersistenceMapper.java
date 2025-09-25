package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.BdrEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BdrPersistenceMapper {

    // Domain -> Entity
    @Mapping(target = "id", ignore = true)
    BdrEntity toEntity(Bdr domain);

    // Entity -> Domain
    Bdr toDomain(BdrEntity entity);
}
