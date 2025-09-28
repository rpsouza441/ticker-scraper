package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.BdrEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.DividendoEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Dividendo;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = { DividendoPersistenceMapper.class },
        config = AtivoFinanceiroMappingConfig.class
)
public interface BdrPersistenceMapper {

    // Domain -> Entity (create)
    @InheritConfiguration(name = "mapCommonToEntity")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dividendos", ignore = true) // coleção tratada manualmente
    BdrEntity toEntity(Bdr source, @Context DividendoPersistenceMapper divMapper);

    @AfterMapping
    default void afterToEntity(Bdr source,
                               @MappingTarget BdrEntity target,
                               @Context DividendoPersistenceMapper divMapper) {
        // Tipo e normalização
        target.setTipoAtivo(TipoAtivo.BDR);
        if (target.getTicker() != null) target.setTicker(target.getTicker().trim().toUpperCase());

        // Replace de dividendos (owner = target)
        List<DividendoEntity> mapped = (source.getDividendos() == null || source.getDividendos().isEmpty())
                ? new ArrayList<>()
                : divMapper.toEntity(source.getDividendos(), target);
        mapped.sort(Comparator.comparing(DividendoEntity::getMes).reversed());
        target.setDividendos(mapped);
    }

    // Entity -> Domain
    @InheritConfiguration(name = "mapCommonToDomain")
    @Mapping(target = "dividendos", ignore = true)
    Bdr toDomain(BdrEntity entity, @Context DividendoPersistenceMapper divMapper);

    @AfterMapping
    default void afterToDomain(BdrEntity source,
                               @MappingTarget Bdr target,
                               @Context DividendoPersistenceMapper divMapper) {
        List<Dividendo> list = (source.getDividendos() == null)
                ? new ArrayList<>()
                : divMapper.toDomain(source.getDividendos());
        list.sort(Comparator.comparing(Dividendo::getMes).reversed());
        target.replaceDividendos(list); // Domínio não expõe setDividendos
        target.setTipoAtivo(TipoAtivo.BDR);
    }

    // Update parcial (coleção tratada fora)
    @InheritConfiguration(name = "updateCommonEntity")
    @Mapping(target = "dividendos", ignore = true)
    void updateEntity(Bdr source, @MappingTarget BdrEntity target);

    // Helper explícito para replace de dividendos na entidade
    default void replaceDividendos(Bdr source, @MappingTarget BdrEntity target,
                                   @Context DividendoPersistenceMapper divMapper) {
        target.getDividendos().clear();
        if (source.getDividendos() != null && !source.getDividendos().isEmpty()) {
            List<DividendoEntity> mapped = divMapper.toEntity(source.getDividendos(), target);
            mapped.sort(Comparator.comparing(DividendoEntity::getMes).reversed());
            target.getDividendos().addAll(mapped);
        }
    }
}
