package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AcaoPersistenceMapper {

    // ------- Entity -> Domain -------
    @Mappings({
            @Mapping(source = "ticker",              target = "ticker"),
            @Mapping(source = "nomeEmpresa",         target = "nomeEmpresa"),
            @Mapping(source = "setor",               target = "setor"),
            @Mapping(source = "segmento",            target = "segmento"),
            @Mapping(source = "segmentoListagem",    target = "segmentoListagem"),
            @Mapping(source = "precoAtual",          target = "precoAtual"),
            @Mapping(source = "variacao12M",         target = "variacao12M"),
            @Mapping(source = "valorMercado",        target = "valorMercado"),
            @Mapping(source = "valorFirma",          target = "valorFirma"),
            @Mapping(source = "patrimonioLiquido",   target = "patrimonioLiquido"),
            @Mapping(source = "numeroTotalPapeis",   target = "numeroTotalPapeis"),
            @Mapping(source = "ativos",              target = "ativos"),
            @Mapping(source = "ativoCirculantes",    target = "ativoCirculantes"),
            @Mapping(source = "dividaBruta",         target = "dividaBruta"),
            @Mapping(source = "dividaLiquida",       target = "dividaLiquida"),
            @Mapping(source = "disponibilidade",     target = "disponibilidade"),
            @Mapping(source = "freeFloat",           target = "freeFloat"),
            @Mapping(source = "tagAlong",            target = "tagAlong"),
            @Mapping(source = "liquidezMediaDiaria", target = "liquidezMediaDiaria"),
            @Mapping(source = "pl",                  target = "pl"),
            @Mapping(source = "psr",                 target = "psr"),
            @Mapping(source = "pvp",                 target = "pvp"),
            @Mapping(source = "dividendYield",       target = "dividendYield"),
            @Mapping(source = "payout",              target = "payout"),
            @Mapping(source = "margemLiquida",       target = "margemLiquida"),
            @Mapping(source = "margemBruta",         target = "margemBruta"),
            @Mapping(source = "margemEbit",          target = "margemEbit"),
            @Mapping(source = "margemEbitda",        target = "margemEbitda"),
            @Mapping(source = "evEbitda",            target = "evEbitda"),
            @Mapping(source = "evEbit",              target = "evEbit"),
            @Mapping(source = "pebitda",             target = "pebitda"),
            @Mapping(source = "pativo",              target = "pativo"),
            @Mapping(source = "pcapitaldeGiro",      target = "pcapitaldeGiro"),
            @Mapping(source = "pativoCirculanteLiquido", target = "pativoCirculanteLiquido"),
            @Mapping(source = "vpa",                 target = "vpa"),
            @Mapping(source = "lpa",                 target = "lpa"),
            @Mapping(source = "giroAtivos",          target = "giroAtivos"),
            @Mapping(source = "roe",                 target = "roe"),
            @Mapping(source = "roic",                target = "roic"),
            @Mapping(source = "roa",                 target = "roa"),
            @Mapping(source = "dividaLiquidaPatrimonio", target = "dividaLiquidaPatrimonio"),
            @Mapping(source = "dividaLiquidaEbitda",     target = "dividaLiquidaEbitda"),
            @Mapping(source = "dividaLiquidaEbit",       target = "dividaLiquidaEbit"),
            @Mapping(source = "dividaBrutaPatrimonio",   target = "dividaBrutaPatrimonio"),
            @Mapping(source = "patrimonioAtivos",        target = "patrimonioAtivos"),
            @Mapping(source = "passivosAtivos",          target = "passivosAtivos"),
            @Mapping(source = "liquidezCorrente",        target = "liquidezCorrente"),
            @Mapping(source = "cagrReceitasCincoAnos",   target = "cagrReceitasCincoAnos"),
            @Mapping(source = "cagrLucrosCincoAnos",     target = "cagrLucrosCincoAnos"),
            @Mapping(source = "dataAtualizacao",         target = "dataAtualizacao")
    })
    Acao toDomain(AcaoEntity entity);

    // ------- Domain -> Entity (CREATE) -------
    @Mappings({
            @Mapping(target = "id",              ignore = true),
            @Mapping(target = "dadosBrutosJson", ignore = true),
            @Mapping(target = "dataAtualizacao", ignore = true),

            @Mapping(source = "ticker",              target = "ticker"),
            @Mapping(source = "nomeEmpresa",         target = "nomeEmpresa"),
            @Mapping(source = "setor",               target = "setor"),
            @Mapping(source = "segmento",            target = "segmento"),
            @Mapping(source = "segmentoListagem",    target = "segmentoListagem"),
            @Mapping(source = "precoAtual",          target = "precoAtual"),
            @Mapping(source = "variacao12M",         target = "variacao12M"),
            @Mapping(source = "valorMercado",        target = "valorMercado"),
            @Mapping(source = "valorFirma",          target = "valorFirma"),
            @Mapping(source = "patrimonioLiquido",   target = "patrimonioLiquido"),
            @Mapping(source = "numeroTotalPapeis",   target = "numeroTotalPapeis"),
            @Mapping(source = "ativos",              target = "ativos"),
            @Mapping(source = "ativoCirculantes",    target = "ativoCirculantes"),
            @Mapping(source = "dividaBruta",         target = "dividaBruta"),
            @Mapping(source = "dividaLiquida",       target = "dividaLiquida"),
            @Mapping(source = "disponibilidade",     target = "disponibilidade"),
            @Mapping(source = "freeFloat",           target = "freeFloat"),
            @Mapping(source = "tagAlong",            target = "tagAlong"),
            @Mapping(source = "liquidezMediaDiaria", target = "liquidezMediaDiaria"),
            @Mapping(source = "pl",                  target = "pl"),
            @Mapping(source = "psr",                 target = "psr"),
            @Mapping(source = "pvp",                 target = "pvp"),
            @Mapping(source = "dividendYield",       target = "dividendYield"),
            @Mapping(source = "payout",              target = "payout"),
            @Mapping(source = "margemLiquida",       target = "margemLiquida"),
            @Mapping(source = "margemBruta",         target = "margemBruta"),
            @Mapping(source = "margemEbit",          target = "margemEbit"),
            @Mapping(source = "margemEbitda",        target = "margemEbitda"),
            @Mapping(source = "evEbitda",            target = "evEbitda"),
            @Mapping(source = "evEbit",              target = "evEbit"),
            @Mapping(source = "pebitda",             target = "pebitda"),
            @Mapping(source = "pativo",              target = "pativo"),
            @Mapping(source = "pcapitaldeGiro",      target = "pcapitaldeGiro"),
            @Mapping(source = "pativoCirculanteLiquido", target = "pativoCirculanteLiquido"),
            @Mapping(source = "vpa",                 target = "vpa"),
            @Mapping(source = "lpa",                 target = "lpa"),
            @Mapping(source = "giroAtivos",          target = "giroAtivos"),
            @Mapping(source = "roe",                 target = "roe"),
            @Mapping(source = "roic",                target = "roic"),
            @Mapping(source = "roa",                 target = "roa"),
            @Mapping(source = "dividaLiquidaPatrimonio", target = "dividaLiquidaPatrimonio"),
            @Mapping(source = "dividaLiquidaEbitda",     target = "dividaLiquidaEbitda"),
            @Mapping(source = "dividaLiquidaEbit",       target = "dividaLiquidaEbit"),
            @Mapping(source = "dividaBrutaPatrimonio",   target = "dividaBrutaPatrimonio"),
            @Mapping(source = "patrimonioAtivos",        target = "patrimonioAtivos"),
            @Mapping(source = "passivosAtivos",          target = "passivosAtivos"),
            @Mapping(source = "liquidezCorrente",        target = "liquidezCorrente"),
            @Mapping(source = "cagrReceitasCincoAnos",   target = "cagrReceitasCincoAnos"),
            @Mapping(source = "cagrLucrosCincoAnos",     target = "cagrLucrosCincoAnos")
    })
    AcaoEntity toEntity(Acao domain);

    // ------- UPDATE escalar (IGNORA nulos) -------
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id",              ignore = true),
            @Mapping(target = "dadosBrutosJson", ignore = true),
            @Mapping(target = "dataAtualizacao", ignore = true)
    })
    void updateEntity(Acao source, @MappingTarget AcaoEntity target);
}
