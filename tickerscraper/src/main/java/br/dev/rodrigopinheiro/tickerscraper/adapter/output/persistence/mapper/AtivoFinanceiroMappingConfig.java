package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AtivoFinanceiroEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.AtivoFinanceiro;
import org.mapstruct.*;

@MapperConfig(componentModel = "spring")
public interface AtivoFinanceiroMappingConfig {

    /** Mapeia campos comuns do domínio -> entidade (criação). Coleções/ID ficam de fora. */
    @BeanMapping(ignoreByDefault = false)
    @Mappings({
            @Mapping(target = "investidorId",   source = "investidorId"),
            @Mapping(target = "ticker",         source = "ticker"),
            @Mapping(target = "nome",           source = "nome"),
            @Mapping(target = "precoAtual",     source = "precoAtual"),
            @Mapping(target = "variacao12M",    source = "variacao12M"),
            @Mapping(target = "dividendYield",  source = "dividendYield"),
            @Mapping(target = "dataAtualizacao",source = "dataAtualizacao"),
            @Mapping(target = "tipoAtivo",      source = "tipoAtivo"),
            // Sempre ignorar estes na base; cada mapper concreto decide o que fazer
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "dividendos", ignore = true)
    })
    void mapCommonToEntity(AtivoFinanceiro source, @MappingTarget AtivoFinanceiroEntity target);

    /** Mapeia campos comuns da entidade -> domínio. Coleções tratadas no mapper concreto. */
    @BeanMapping(ignoreByDefault = false)
    @Mappings({
            @Mapping(target = "investidorId",   source = "investidorId"),
            @Mapping(target = "ticker",         source = "ticker"),
            @Mapping(target = "nome",           source = "nome"),
            @Mapping(target = "precoAtual",     source = "precoAtual"),
            @Mapping(target = "variacao12M",    source = "variacao12M"),
            @Mapping(target = "dividendYield",  source = "dividendYield"),
            @Mapping(target = "dataAtualizacao",source = "dataAtualizacao"),
            @Mapping(target = "tipoAtivo",      source = "tipoAtivo")
            // NÃO mapeie dividendos aqui — o concreto faz o replace/ordem
    })
    void mapCommonToDomain(AtivoFinanceiroEntity source, @MappingTarget AtivoFinanceiro target);

    /** Update parcial: ignora nulos do domínio. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "dividendos", ignore = true)
    })
    void updateCommonEntity(AtivoFinanceiro source, @MappingTarget AtivoFinanceiroEntity target);
}
