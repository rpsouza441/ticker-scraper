package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrHistoricalIndicatorEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.HistoricalIndicator;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BdrHistoricalIndicatorMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "bdr", ignore = true)
    })
    BdrHistoricalIndicatorEntity toEntity(HistoricalIndicator indicator);

    @InheritInverseConfiguration
    HistoricalIndicator toDomain(BdrHistoricalIndicatorEntity entity);

    List<BdrHistoricalIndicatorEntity> toEntityList(List<HistoricalIndicator> source);

    List<HistoricalIndicator> toDomainList(List<BdrHistoricalIndicatorEntity> source);
}
