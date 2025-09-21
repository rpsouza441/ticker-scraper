package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.EtfEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Etf;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EtfEntityMapper {
    
    @Mapping(target = "ticker", source = "ticker")
    @Mapping(target = "tipoAtivo", source = "tipoAtivo")
    @Mapping(target = "nomeEtf", source = "nomeEtf")
    @Mapping(target = "valorAtual", source = "valorAtual")
    @Mapping(target = "capitalizacao", source = "capitalizacao")
    @Mapping(target = "variacao12M", source = "variacao12M")
    @Mapping(target = "variacao60M", source = "variacao60M")
    @Mapping(target = "dy", source = "dy")
    Etf toDomain(EtfEntity entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticker", source = "ticker")
    @Mapping(target = "tipoAtivo", source = "tipoAtivo")
    @Mapping(target = "nomeEtf", source = "nomeEtf")
    @Mapping(target = "valorAtual", source = "valorAtual")
    @Mapping(target = "capitalizacao", source = "capitalizacao")
    @Mapping(target = "variacao12M", source = "variacao12M")
    @Mapping(target = "variacao60M", source = "variacao60M")
    @Mapping(target = "dy", source = "dy")
    @Mapping(target = "dadosBrutosJson", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    EtfEntity toEntity(Etf domain);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "dadosBrutosJson", ignore = true)
    void updateEntity(@MappingTarget EtfEntity entity, Etf domain);
}