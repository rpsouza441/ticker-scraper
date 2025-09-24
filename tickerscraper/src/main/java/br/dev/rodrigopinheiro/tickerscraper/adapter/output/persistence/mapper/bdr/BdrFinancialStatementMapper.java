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

    default QualityValueEmbeddable toEmbeddable(QualityValue value) {
        if (value == null) {
            return null;
        }
        QualityValueEmbeddable embeddable = new QualityValueEmbeddable();
        embeddable.setValor(value.getValor());
        embeddable.setQuality(value.getQuality());
        embeddable.setRaw(value.getRaw());
        return embeddable;
    }

    default QualityValue toDomain(QualityValueEmbeddable value) {
        if (value == null) {
            return null;
        }
        QualityValue qualityValue = new QualityValue();
        qualityValue.setValor(value.getValor());
        qualityValue.setQuality(value.getQuality());
        qualityValue.setRaw(value.getRaw());
        return qualityValue;
    }
}
