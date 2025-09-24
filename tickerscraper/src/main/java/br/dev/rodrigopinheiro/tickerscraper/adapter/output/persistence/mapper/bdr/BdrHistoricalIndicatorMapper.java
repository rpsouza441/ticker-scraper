package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrHistoricalIndicatorEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.HistoricalIndicator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BdrHistoricalIndicatorMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "bdr", ignore = true),
            @Mapping(source = "year", target = "year"),
            @Mapping(source = "pl", target = "pl"),
            @Mapping(source = "pvp", target = "pvp"),
            @Mapping(source = "psr", target = "psr"),
            @Mapping(source = "pEbit", target = "pEbit"),
            @Mapping(source = "pEbitda", target = "pEbitda"),
            @Mapping(source = "pAtivo", target = "pAtivo"),
            @Mapping(source = "roe", target = "roe"),
            @Mapping(source = "roic", target = "roic"),
            @Mapping(source = "roa", target = "roa"),
            @Mapping(source = "margemBruta", target = "margemBruta"),
            @Mapping(source = "margemOperacional", target = "margemOperacional"),
            @Mapping(source = "margemLiquida", target = "margemLiquida"),
            @Mapping(source = "vpa", target = "vpa"),
            @Mapping(source = "lpa", target = "lpa"),
            @Mapping(source = "patrimonioPorAtivos", target = "patrimonioPorAtivos")
    })
    BdrHistoricalIndicatorEntity toEntity(HistoricalIndicator indicator);

    @Mappings({
            @Mapping(source = "year", target = "year"),
            @Mapping(source = "pl", target = "pl"),
            @Mapping(source = "pvp", target = "pvp"),
            @Mapping(source = "psr", target = "psr"),
            @Mapping(source = "pEbit", target = "pEbit"),
            @Mapping(source = "pEbitda", target = "pEbitda"),
            @Mapping(source = "pAtivo", target = "pAtivo"),
            @Mapping(source = "roe", target = "roe"),
            @Mapping(source = "roic", target = "roic"),
            @Mapping(source = "roa", target = "roa"),
            @Mapping(source = "margemBruta", target = "margemBruta"),
            @Mapping(source = "margemOperacional", target = "margemOperacional"),
            @Mapping(source = "margemLiquida", target = "margemLiquida"),
            @Mapping(source = "vpa", target = "vpa"),
            @Mapping(source = "lpa", target = "lpa"),
            @Mapping(source = "patrimonioPorAtivos", target = "patrimonioPorAtivos")
    })
    HistoricalIndicator toDomain(BdrHistoricalIndicatorEntity entity);

    default List<BdrHistoricalIndicatorEntity> toEntityList(List<HistoricalIndicator> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        return source.stream()
                .map(this::toEntity)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    default List<HistoricalIndicator> toDomainList(List<BdrHistoricalIndicatorEntity> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        return source.stream()
                .map(this::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
