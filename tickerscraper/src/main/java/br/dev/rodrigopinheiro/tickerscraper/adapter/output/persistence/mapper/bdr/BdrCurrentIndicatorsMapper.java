package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrCurrentIndicatorsEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.CurrentIndicators;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BdrCurrentIndicatorsMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "bdr", ignore = true)
    })
    BdrCurrentIndicatorsEntity toEntity(CurrentIndicators indicators);

    @InheritInverseConfiguration
    CurrentIndicators toDomain(BdrCurrentIndicatorsEntity entity);
}
