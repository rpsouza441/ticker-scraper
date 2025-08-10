package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FiiDividendoEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FiiDividendo;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FiiPersistenceMapper {

    // ===== Entity -> Domain =====
    @Mappings({
            @Mapping(source = "ticker",                    target = "ticker"),
            @Mapping(source = "nomeEmpresa",               target = "nomeEmpresa"),
            @Mapping(source = "razaoSocial",               target = "razaoSocial"),
            @Mapping(source = "cnpj",                      target = "cnpj"),
            @Mapping(source = "publicoAlvo",               target = "publicoAlvo"),
            @Mapping(source = "mandato",                   target = "mandato"),
            @Mapping(source = "segmento",                  target = "segmento"),
            @Mapping(source = "tipoDeFundo",               target = "tipoDeFundo"),
            @Mapping(source = "prazoDeDuracao",            target = "prazoDeDuracao"),
            @Mapping(source = "tipoDeGestao",              target = "tipoDeGestao"),
            @Mapping(source = "taxaDeAdministracao",       target = "taxaDeAdministracao"),
            @Mapping(source = "ultimoRendimento",          target = "ultimoRendimento"),
            @Mapping(source = "cotacao",                   target = "cotacao"),
            @Mapping(source = "valorDeMercado",            target = "valorDeMercado"),
            @Mapping(source = "pvp",                       target = "pvp"),
            @Mapping(source = "dividendYield",             target = "dividendYield"),
            @Mapping(source = "liquidezDiaria",            target = "liquidezDiaria"),
            @Mapping(source = "valorPatrimonial",          target = "valorPatrimonial"),
            @Mapping(source = "valorPatrimonialPorCota",   target = "valorPatrimonialPorCota"),
            @Mapping(source = "vacancia",                  target = "vacancia"),
            @Mapping(source = "numeroDeCotistas",          target = "numeroDeCotistas"),
            @Mapping(source = "cotasEmitidas",             target = "cotasEmitidas"),
            @Mapping(source = "fiiDividendos",             target = "fiiDividendos")
            // internalId, dadosBrutosJson, dataAtualizacao -> não existem no domínio
    })
    FundoImobiliario toDomain(FundoImobiliarioEntity entity);

    // ===== Domain -> Entity =====
    @InheritInverseConfiguration(name = "toDomain")
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "internalId", ignore = true),      // setar no service
            @Mapping(target = "dadosBrutosJson", ignore = true),
            @Mapping(target = "dataAtualizacao", ignore = true),
            @Mapping(target = "fiiDividendos", ignore = true)    // setado no service (back-ref)
    })
    FundoImobiliarioEntity toEntity(FundoImobiliario domain);

    // ===== Dividendos =====
    @Mappings({
            @Mapping(target = "mes", source = "mes", qualifiedByName = "ymToLocalDate")
    })
    FiiDividendoEntity toEntity(FiiDividendo domain);

    @Mappings({
            @Mapping(target = "mes", source = "mes", qualifiedByName = "localDateToYm")
    })
    FiiDividendo toDomain(FiiDividendoEntity entity);

    List<FiiDividendoEntity> toEntityList(List<FiiDividendo> domain);
    List<FiiDividendo> toDomainList(List<FiiDividendoEntity> entities);

    // ===== Helpers de conversão =====
    @Named("ymToLocalDate")
    default LocalDate ymToLocalDate(YearMonth ym) {
        return ym == null ? null : ym.atDay(1);
    }

    @Named("localDateToYm")
    default YearMonth localDateToYm(LocalDate date) {
        return date == null ? null : YearMonth.from(date);
    }
}
