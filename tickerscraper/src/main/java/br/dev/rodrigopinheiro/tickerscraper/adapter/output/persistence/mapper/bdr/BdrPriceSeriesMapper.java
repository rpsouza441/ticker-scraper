package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrPriceSeriesEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.PricePoint;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring", uses = TimeMapper.class)
public interface BdrPriceSeriesMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "bdr", ignore = true),
            @Mapping(source = "date", target = "dt"),
            @Mapping(source = "close", target = "close")
    })
    BdrPriceSeriesEntity toEntity(PricePoint point);

    @InheritInverseConfiguration
    PricePoint toDomain(BdrPriceSeriesEntity entity);

    List<BdrPriceSeriesEntity> toEntityList(List<PricePoint> source);

    List<PricePoint> toDomainList(List<BdrPriceSeriesEntity> source);
}
