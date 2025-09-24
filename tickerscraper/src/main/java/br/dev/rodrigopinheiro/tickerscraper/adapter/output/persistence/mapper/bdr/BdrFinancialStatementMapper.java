package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.*;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.BpYear;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.DreYear;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.FcYear;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BdrFinancialStatementMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "bdr", ignore = true),
            @Mapping(source = "receitaTotalUsd", target = "receitaTotalUsd"),
            @Mapping(source = "lucroBrutoUsd", target = "lucroBrutoUsd"),
            @Mapping(source = "ebitdaUsd", target = "ebitdaUsd"),
            @Mapping(source = "ebitUsd", target = "ebitUsd"),
            @Mapping(source = "lucroLiquidoUsd", target = "lucroLiquidoUsd")
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

    default QualityMetricEmbeddable toEmbeddable(DreYear.Metric metric) {
        if (metric == null) {
            return null;
        }
        QualityMetricEmbeddable embeddable = new QualityMetricEmbeddable();
        embeddable.setValue(metric.getValue());
        embeddable.setQuality(metric.getQuality());
        embeddable.setRaw(metric.getRaw());
        return embeddable;
    }

    default DreYear.Metric toDomain(QualityMetricEmbeddable embeddable) {
        if (embeddable == null) {
            return null;
        }
        DreYear.Metric metric = new DreYear.Metric();
        metric.setValue(embeddable.getValue());
        metric.setQuality(embeddable.getQuality());
        metric.setRaw(embeddable.getRaw());
        return metric;
    }
}
