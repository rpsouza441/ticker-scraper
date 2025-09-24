package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.*;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.*;
import org.hibernate.LazyInitializationException;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring",
        uses = {
                TimeMapper.class,
                BdrPriceSeriesMapper.class,
                BdrDividendYearMapper.class,
                BdrHistoricalIndicatorMapper.class,
                BdrFinancialStatementMapper.class,
                BdrCurrentIndicatorsMapper.class,
                BdrParidadeMapper.class,
                BdrMarketCapMapper.class
        })
public interface BdrMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "ticker", target = "ticker"),
            @Mapping(source = "tipoAtivo", target = "tipoAtivo"),
            @Mapping(source = "investidorId", target = "investidorId"),
            @Mapping(source = "nomeBdr", target = "nomeBdr"),
            @Mapping(source = "setor", target = "setor"),
            @Mapping(source = "industria", target = "industria"),
            @Mapping(source = "priceCurrency", target = "priceCurrency"),
            @Mapping(source = "financialsCurrency", target = "financialsCurrency"),
            @Mapping(source = "cotacao", target = "cotacao"),
            @Mapping(source = "variacao12", target = "variacao12"),
            @Mapping(source = "priceSeries", target = "priceSeries"),
            @Mapping(source = "dividendYears", target = "dividendYears"),
            @Mapping(source = "historicalIndicators", target = "historicalIndicators"),
            @Mapping(source = "dreYears", target = "dreYears"),
            @Mapping(source = "bpYears", target = "bpYears"),
            @Mapping(source = "fcYears", target = "fcYears"),
            @Mapping(source = "currentIndicators", target = "currentIndicators"),
            @Mapping(source = "paridade", target = "paridade"),
            @Mapping(source = "marketCap", target = "marketCap"),
            @Mapping(source = "updatedAt", target = "updatedAt")
    })
    BdrEntity toEntity(Bdr bdr);

    @InheritInverseConfiguration
    @Mappings({
            @Mapping(source = "priceSeries", target = "priceSeries"),
            @Mapping(source = "dividendYears", target = "dividendYears"),
            @Mapping(source = "historicalIndicators", target = "historicalIndicators"),
            @Mapping(source = "dreYears", target = "dreYears"),
            @Mapping(source = "bpYears", target = "bpYears"),
            @Mapping(source = "fcYears", target = "fcYears")
    })
    Bdr toDomain(BdrEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "priceSeries", ignore = true),
            @Mapping(target = "dividendYears", ignore = true),
            @Mapping(target = "historicalIndicators", ignore = true),
            @Mapping(target = "dreYears", ignore = true),
            @Mapping(target = "bpYears", ignore = true),
            @Mapping(target = "fcYears", ignore = true),
            @Mapping(target = "currentIndicators", ignore = true),
            @Mapping(target = "paridade", ignore = true),
            @Mapping(target = "marketCap", ignore = true)
    })
    void updateEntity(Bdr source, @MappingTarget BdrEntity target);

    default void replaceAssociations(Bdr source, @MappingTarget BdrEntity target) {
        replacePriceSeries(source, target);
        replaceDividendYears(source, target);
        replaceHistoricalIndicators(source, target);
        replaceDreYears(source, target);
        replaceBpYears(source, target);
        replaceFcYears(source, target);
        replaceCurrentIndicators(source, target);
        replaceParidade(source, target);
        replaceMarketCap(source, target);
    }

    default void replacePriceSeries(Bdr source, BdrEntity target) {
        if (target.getPriceSeries() == null) {
            target.setPriceSeries(new ArrayList<>());
        } else {
            target.getPriceSeries().clear();
        }
        List<PricePoint> pricePoints = source.getPriceSeries();
        if (pricePoints != null) {
            List<BdrPriceSeriesEntity> entities = toPriceSeriesEntities(pricePoints);
            for (BdrPriceSeriesEntity entity : entities) {
                entity.setBdr(target);
                target.getPriceSeries().add(entity);
            }
        }
    }

    default void replaceDividendYears(Bdr source, BdrEntity target) {
        if (target.getDividendYears() == null) {
            target.setDividendYears(new ArrayList<>());
        } else {
            target.getDividendYears().clear();
        }
        List<DividendYear> dividendYears = source.getDividendYears();
        if (dividendYears != null) {
            List<BdrDividendYearlyEntity> entities = toDividendYearEntities(dividendYears);
            for (BdrDividendYearlyEntity entity : entities) {
                entity.setBdr(target);
                target.getDividendYears().add(entity);
            }
        }
    }

    default void replaceHistoricalIndicators(Bdr source, BdrEntity target) {
        if (target.getHistoricalIndicators() == null) {
            target.setHistoricalIndicators(new ArrayList<>());
        } else {
            target.getHistoricalIndicators().clear();
        }
        List<HistoricalIndicator> indicators = source.getHistoricalIndicators();
        if (indicators != null) {
            List<BdrHistoricalIndicatorEntity> entities = toHistoricalIndicatorEntities(indicators);
            for (BdrHistoricalIndicatorEntity entity : entities) {
                entity.setBdr(target);
                target.getHistoricalIndicators().add(entity);
            }
        }
    }

    default void replaceDreYears(Bdr source, BdrEntity target) {
        if (target.getDreYears() == null) {
            target.setDreYears(new ArrayList<>());
        } else {
            target.getDreYears().clear();
        }
        List<DreYear> dreYears = source.getDreYears();
        if (dreYears != null) {
            List<BdrDreYearEntity> entities = toDreEntities(dreYears);
            for (BdrDreYearEntity entity : entities) {
                entity.setBdr(target);
                target.getDreYears().add(entity);
            }
        }
    }

    default void replaceBpYears(Bdr source, BdrEntity target) {
        if (target.getBpYears() == null) {
            target.setBpYears(new ArrayList<>());
        } else {
            target.getBpYears().clear();
        }
        List<BpYear> bpYears = source.getBpYears();
        if (bpYears != null) {
            List<BdrBpYearEntity> entities = toBpEntities(bpYears);
            for (BdrBpYearEntity entity : entities) {
                entity.setBdr(target);
                target.getBpYears().add(entity);
            }
        }
    }

    default void replaceFcYears(Bdr source, BdrEntity target) {
        if (target.getFcYears() == null) {
            target.setFcYears(new ArrayList<>());
        } else {
            target.getFcYears().clear();
        }
        List<FcYear> fcYears = source.getFcYears();
        if (fcYears != null) {
            List<BdrFcYearEntity> entities = toFcEntities(fcYears);
            for (BdrFcYearEntity entity : entities) {
                entity.setBdr(target);
                target.getFcYears().add(entity);
            }
        }
    }

    default void replaceCurrentIndicators(Bdr source, BdrEntity target) {
        CurrentIndicators indicators = source.getCurrentIndicators();
        if (indicators == null) {
            target.setCurrentIndicators(null);
            return;
        }
        BdrCurrentIndicatorsEntity entity = toCurrentIndicatorsEntity(indicators);
        entity.setBdr(target);
        target.setCurrentIndicators(entity);
    }

    default void replaceParidade(Bdr source, BdrEntity target) {
        ParidadeBdr paridade = source.getParidade();
        if (paridade == null) {
            target.setParidade(null);
            return;
        }
        BdrParidadeEntity entity = toParidadeEntity(paridade);
        entity.setBdr(target);
        target.setParidade(entity);
    }

    default void replaceMarketCap(Bdr source, BdrEntity target) {
        BdrMarketCap marketCap = source.getMarketCap();
        if (marketCap == null) {
            target.setMarketCap(null);
            return;
        }
        BdrMarketCapEntity entity = toMarketCapEntity(marketCap);
        entity.setBdr(target);
        target.setMarketCap(entity);
    }

    List<BdrPriceSeriesEntity> toPriceSeriesEntities(List<PricePoint> source);

    List<BdrDividendYearlyEntity> toDividendYearEntities(List<DividendYear> source);

    List<BdrHistoricalIndicatorEntity> toHistoricalIndicatorEntities(List<HistoricalIndicator> source);

    List<BdrDreYearEntity> toDreEntities(List<DreYear> source);

    List<BdrBpYearEntity> toBpEntities(List<BpYear> source);

    List<BdrFcYearEntity> toFcEntities(List<FcYear> source);

    BdrCurrentIndicatorsEntity toCurrentIndicatorsEntity(CurrentIndicators indicators);

    BdrParidadeEntity toParidadeEntity(ParidadeBdr paridade);

    BdrMarketCapEntity toMarketCapEntity(BdrMarketCap marketCap);

    @AfterMapping
    default void wireParents(@MappingTarget BdrEntity entity) {
        try {
            if (entity.getPriceSeries() != null) {
                for (BdrPriceSeriesEntity child : entity.getPriceSeries()) {
                    child.setBdr(entity);
                }
            }
        } catch (LazyInitializationException ignored) {
        }
        try {
            if (entity.getDividendYears() != null) {
                for (BdrDividendYearlyEntity child : entity.getDividendYears()) {
                    child.setBdr(entity);
                }
            }
        } catch (LazyInitializationException ignored) {
        }
        try {
            if (entity.getHistoricalIndicators() != null) {
                for (BdrHistoricalIndicatorEntity child : entity.getHistoricalIndicators()) {
                    child.setBdr(entity);
                }
            }
        } catch (LazyInitializationException ignored) {
        }
        try {
            if (entity.getDreYears() != null) {
                for (BdrDreYearEntity child : entity.getDreYears()) {
                    child.setBdr(entity);
                }
            }
        } catch (LazyInitializationException ignored) {
        }
        try {
            if (entity.getBpYears() != null) {
                for (BdrBpYearEntity child : entity.getBpYears()) {
                    child.setBdr(entity);
                }
            }
        } catch (LazyInitializationException ignored) {
        }
        try {
            if (entity.getFcYears() != null) {
                for (BdrFcYearEntity child : entity.getFcYears()) {
                    child.setBdr(entity);
                }
            }
        } catch (LazyInitializationException ignored) {
        }
        if (entity.getCurrentIndicators() != null) {
            entity.getCurrentIndicators().setBdr(entity);
        }
        if (entity.getParidade() != null) {
            entity.getParidade().setBdr(entity);
        }
        if (entity.getMarketCap() != null) {
            entity.getMarketCap().setBdr(entity);
        }
    }
}
