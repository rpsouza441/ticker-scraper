package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrDividendYearlyEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.DividendYear;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BdrDividendYearMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "bdr", ignore = true),
            @Mapping(source = "year", target = "year"),
            @Mapping(source = "valor", target = "valor"),
            @Mapping(source = "dividendYield", target = "dividendYield"),
            @Mapping(source = "currency", target = "currency")
    })
    BdrDividendYearlyEntity toEntity(DividendYear year);

    @Mappings({
            @Mapping(source = "year", target = "year"),
            @Mapping(source = "valor", target = "valor"),
            @Mapping(source = "dividendYield", target = "dividendYield"),
            @Mapping(source = "currency", target = "currency")
    })
    DividendYear toDomain(BdrDividendYearlyEntity entity);

    default List<BdrDividendYearlyEntity> toEntityList(List<DividendYear> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        return source.stream()
                .filter(Objects::nonNull)
                .map(this::toEntity)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    default List<DividendYear> toDomainList(List<BdrDividendYearlyEntity> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        return source.stream()
                .filter(Objects::nonNull)
                .map(this::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
