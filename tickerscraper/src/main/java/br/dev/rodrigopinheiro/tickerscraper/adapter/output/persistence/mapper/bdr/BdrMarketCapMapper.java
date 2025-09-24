package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrMarketCapEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.BdrMarketCap;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BdrMarketCapMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "bdr", ignore = true)
    })
    BdrMarketCapEntity toEntity(BdrMarketCap marketCap);

    @InheritInverseConfiguration
    BdrMarketCap toDomain(BdrMarketCapEntity entity);
}
