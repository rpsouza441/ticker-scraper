package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;


import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrBpYearEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrDreYearEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.BdrFcYearEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.QualityValueEmbeddable;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.AuditedBigDecimalEmbeddable;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.QualityMetricEmbeddable;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.AuditedValue;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.BpYear;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.DreYear;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.FcYear;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.QualityValue;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BdrFinancialStatementMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bdr", ignore = true)
    BdrDreYearEntity toDreEntity(DreYear year);


    @InheritInverseConfiguration(name = "toDreEntity")
    DreYear toDreDomain(BdrDreYearEntity entity);

    List<BdrDreYearEntity> toDreEntityList(List<DreYear> source);

    List<DreYear> toDreDomainList(List<BdrDreYearEntity> source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bdr", ignore = true)
    BdrBpYearEntity toBpEntity(BpYear year);

    @InheritInverseConfiguration(name = "toBpEntity")
    BpYear toBpDomain(BdrBpYearEntity entity);

    List<BdrBpYearEntity> toBpEntityList(List<BpYear> source);

    List<BpYear> toBpDomainList(List<BdrBpYearEntity> source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bdr", ignore = true)
    BdrFcYearEntity toFcEntity(FcYear year);


    @InheritInverseConfiguration(name = "toFcEntity")
    FcYear toFcDomain(BdrFcYearEntity entity);

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

    default DreYear.Metric toMetric(QualityMetricEmbeddable embeddable) {
        if (embeddable == null) {
            return null;
        }
        DreYear.Metric metric = new DreYear.Metric();
        metric.setValue(embeddable.getValue());
        metric.setQuality(embeddable.getQuality());
        metric.setRaw(embeddable.getRaw());
        return metric;
    }

    default QualityValueEmbeddable toEmbeddable(QualityValue value) {
        if (value == null) {
            return null;
        }
        QualityValueEmbeddable embeddable = new QualityValueEmbeddable();
        embeddable.setValue(value.getValue());
        embeddable.setQuality(value.getQuality());
        embeddable.setRaw(value.getRaw());
        return embeddable;
    }

    default QualityValue toDomain(QualityValueEmbeddable value) {
        if (value == null) {
            return null;
        }
        QualityValue qualityValue = new QualityValue();
        qualityValue.setValue(value.getValue());
        qualityValue.setQuality(value.getQuality());
        qualityValue.setRaw(value.getRaw());
        return qualityValue;

    }

    default AuditedBigDecimalEmbeddable toEmbeddable(AuditedValue value) {
        if (value == null) {
            return null;
        }
        AuditedBigDecimalEmbeddable embeddable = new AuditedBigDecimalEmbeddable();
        embeddable.setValue(value.getValue());
        embeddable.setQuality(value.getQuality());
        embeddable.setRaw(value.getRaw());
        return embeddable;
    }

    default AuditedValue toDomain(AuditedBigDecimalEmbeddable embeddable) {
        if (embeddable == null) {
            return null;
        }
        AuditedValue value = new AuditedValue();
        value.setValue(embeddable.getValue());
        value.setQuality(embeddable.getQuality());
        value.setRaw(embeddable.getRaw());
        return value;
    }
}
