package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrBpYearEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrDreYearEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrFcYearEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.QualityValueEmbeddable;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.BpYear;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.DreYear;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.FcYear;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.QualityValue;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BdrFinancialStatementMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "bdr", ignore = true)
    })
    BdrDreYearEntity toEntity(DreYear year);

    @InheritInverseConfiguration
    DreYear toDomain(BdrDreYearEntity entity);

    List<BdrDreYearEntity> toDreEntityList(List<DreYear> source);

    List<DreYear> toDreDomainList(List<BdrDreYearEntity> source);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "bdr", ignore = true)
    })
    BdrBpYearEntity toEntity(BpYear year);

    @InheritInverseConfiguration
    BpYear toDomain(BdrBpYearEntity entity);

    List<BdrBpYearEntity> toBpEntityList(List<BpYear> source);

    List<BpYear> toBpDomainList(List<BdrBpYearEntity> source);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "bdr", ignore = true)
    })
    BdrFcYearEntity toEntity(FcYear year);

    @InheritInverseConfiguration
    FcYear toDomain(BdrFcYearEntity entity);

    List<BdrFcYearEntity> toFcEntityList(List<FcYear> source);

    List<FcYear> toFcDomainList(List<BdrFcYearEntity> source);

    QualityValueEmbeddable toEntity(QualityValue value);

    QualityValue toDomain(QualityValueEmbeddable value);
}
