package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AtivoFinanceiroEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.DividendoEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Dividendo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DividendoPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true) // setado pelo helper com owner
    DividendoEntity toEntity(Dividendo source);

    Dividendo toDomain(DividendoEntity entity);

    // --------- Helpers de lista com owner ---------

    default List<DividendoEntity> toEntity(List<Dividendo> source, AtivoFinanceiroEntity owner) {
        List<DividendoEntity> out = new ArrayList<>();
        if (source == null) return out;
        for (Dividendo d : source) {
            DividendoEntity e = toEntity(d);
            e.setAtivo(owner);     // Owner correto para FK ativo_id
            out.add(e);
        }
        return out;
    }

    default List<Dividendo> toDomain(List<DividendoEntity> entities) {
        List<Dividendo> out = new ArrayList<>();
        if (entities == null) return out;
        for (DividendoEntity e : entities) {
            out.add(toDomain(e));
        }
        return out;
    }
}
