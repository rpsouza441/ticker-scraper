package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AcaoPersistenceMapper {

    Acao toDomain(AcaoEntity entity);

    AcaoEntity toEntity(Acao domain);
}
