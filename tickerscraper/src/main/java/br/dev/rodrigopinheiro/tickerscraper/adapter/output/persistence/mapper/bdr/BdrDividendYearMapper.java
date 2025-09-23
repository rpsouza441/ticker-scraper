package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrDividendYearlyEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.DividendYear;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BdrDividendYearMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "bdr", ignore = true),
            @Mapping(source = "ano", target = "ano"),
            @Mapping(source = "totalDividendo", target = "totalDividendo"),
            @Mapping(source = "dividendYield", target = "dividendYield")
    })
    BdrDividendYearlyEntity toEntity(DividendYear year);

    @InheritInverseConfiguration
    DividendYear toDomain(BdrDividendYearlyEntity entity);

    List<BdrDividendYearlyEntity> toEntityList(List<DividendYear> source);

    List<DividendYear> toDomainList(List<BdrDividendYearlyEntity> source);
}
