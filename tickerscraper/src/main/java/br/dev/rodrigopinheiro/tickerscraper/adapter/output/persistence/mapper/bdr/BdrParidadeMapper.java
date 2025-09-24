package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrParidadeEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.ParidadeBdr;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BdrParidadeMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "bdr", ignore = true),
            @Mapping(source = "ratio", target = "value")
    })
    BdrParidadeEntity toEntity(ParidadeBdr paridade);

    @InheritInverseConfiguration
    ParidadeBdr toDomain(BdrParidadeEntity entity);
}
