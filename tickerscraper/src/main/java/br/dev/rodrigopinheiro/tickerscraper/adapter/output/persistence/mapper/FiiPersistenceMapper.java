package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FiiDividendoEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FiiDividendo;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FiiPersistenceMapper {

    // -------- Entity -> Domain (READ) --------
    @Mappings({
            @Mapping(source = "ticker",                  target = "ticker"),
            @Mapping(source = "nomeEmpresa",             target = "nomeEmpresa"),
            @Mapping(source = "razaoSocial",             target = "razaoSocial"),
            @Mapping(source = "cnpj",                    target = "cnpj"),
            @Mapping(source = "publicoAlvo",             target = "publicoAlvo"),
            @Mapping(source = "mandato",                 target = "mandato"),
            @Mapping(source = "segmento",                target = "segmento"),
            @Mapping(source = "tipoDeFundo",             target = "tipoDeFundo"),
            @Mapping(source = "prazoDeDuracao",          target = "prazoDeDuracao"),
            @Mapping(source = "tipoDeGestao",            target = "tipoDeGestao"),
            @Mapping(source = "taxaDeAdministracao",     target = "taxaDeAdministracao"),
            @Mapping(source = "ultimoRendimento",        target = "ultimoRendimento"),
            @Mapping(source = "cotacao",                 target = "cotacao"),
            @Mapping(source = "variacao12M",             target = "variacao12M"),
            @Mapping(source = "valorDeMercado",          target = "valorDeMercado"),
            @Mapping(source = "pvp",                     target = "pvp"),
            @Mapping(source = "dividendYield",           target = "dividendYield"),
            @Mapping(source = "liquidezDiaria",          target = "liquidezDiaria"),
            @Mapping(source = "valorPatrimonial",        target = "valorPatrimonial"),
            @Mapping(source = "valorPatrimonialPorCota", target = "valorPatrimonialPorCota"),
            @Mapping(source = "vacancia",                target = "vacancia"),
            @Mapping(source = "numeroDeCotistas",        target = "numeroDeCotistas"),
            @Mapping(source = "cotasEmitidas",           target = "cotasEmitidas"),
            @Mapping(source = "fiiDividendos",           target = "fiiDividendos")
    })
    FundoImobiliario toDomain(FundoImobiliarioEntity entity);

    @Mappings({
            @Mapping(source = "mes",   target = "mes"),   // LocalDate -> YearMonth via conversor abaixo
            @Mapping(source = "valor", target = "valor")
    })
    FiiDividendo toDomain(FiiDividendoEntity e);

    // -------- Domain -> Entity (CREATE) --------
    @Mappings({
            @Mapping(target = "id",               ignore = true),
            @Mapping(target = "internalId",       ignore = true),
            @Mapping(target = "dadosBrutosJson",  ignore = true),
            @Mapping(target = "dataAtualizacao",  ignore = true),
            @Mapping(source = "ticker",                  target = "ticker"),
            @Mapping(source = "nomeEmpresa",             target = "nomeEmpresa"),
            @Mapping(source = "razaoSocial",             target = "razaoSocial"),
            @Mapping(source = "cnpj",                    target = "cnpj"),
            @Mapping(source = "publicoAlvo",             target = "publicoAlvo"),
            @Mapping(source = "mandato",                 target = "mandato"),
            @Mapping(source = "segmento",                target = "segmento"),
            @Mapping(source = "tipoDeFundo",             target = "tipoDeFundo"),
            @Mapping(source = "prazoDeDuracao",          target = "prazoDeDuracao"),
            @Mapping(source = "tipoDeGestao",            target = "tipoDeGestao"),
            @Mapping(source = "taxaDeAdministracao",     target = "taxaDeAdministracao"),
            @Mapping(source = "ultimoRendimento",        target = "ultimoRendimento"),
            @Mapping(source = "cotacao",                 target = "cotacao"),
            @Mapping(source = "variacao12M",             target = "variacao12M"),
            @Mapping(source = "valorDeMercado",          target = "valorDeMercado"),
            @Mapping(source = "pvp",                     target = "pvp"),
            @Mapping(source = "dividendYield",           target = "dividendYield"),
            @Mapping(source = "liquidezDiaria",          target = "liquidezDiaria"),
            @Mapping(source = "valorPatrimonial",        target = "valorPatrimonial"),
            @Mapping(source = "valorPatrimonialPorCota", target = "valorPatrimonialPorCota"),
            @Mapping(source = "vacancia",                target = "vacancia"),
            @Mapping(source = "numeroDeCotistas",        target = "numeroDeCotistas"),
            @Mapping(source = "cotasEmitidas",           target = "cotasEmitidas"),
            @Mapping(source = "fiiDividendos",           target = "fiiDividendos")
    })
    FundoImobiliarioEntity toEntity(FundoImobiliario domain);

    @Mappings({
            @Mapping(target = "id",               ignore = true),
            @Mapping(target = "fundoImobiliario", ignore = true),
            @Mapping(source = "mes",   target = "mes"),   // YearMonth -> LocalDate via conversor abaixo
            @Mapping(source = "valor", target = "valor")
    })
    FiiDividendoEntity toEntity(FiiDividendo d);

    // -------- UPDATE escalar (IGNORA nulos) --------
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id",               ignore = true),
            @Mapping(target = "internalId",       ignore = true),
            @Mapping(target = "dadosBrutosJson",  ignore = true),
            @Mapping(target = "dataAtualizacao",  ignore = true),
            @Mapping(target = "fiiDividendos",    ignore = true) // coleção tratada à parte
    })
    void updateEntity(FundoImobiliario source, @MappingTarget FundoImobiliarioEntity target);

    // -------- Coleção: wipe & recreate (12 meses exatos) --------
    default void replaceDividendos(FundoImobiliario source, @MappingTarget FundoImobiliarioEntity target) {
        if (target.getFiiDividendos() == null) {
            target.setFiiDividendos(new ArrayList<>());
        } else {
            target.getFiiDividendos().clear(); // orphanRemoval=true -> DELETE
        }
        if (source.getFiiDividendos() != null) {
            for (FiiDividendo d : source.getFiiDividendos()) {
                FiiDividendoEntity e = toEntity(d);
                e.setFundoImobiliario(target); // back-ref (FK)
                target.getFiiDividendos().add(e);
            }
        }
    }

    // -------- Back-ref quando criar tudo de uma vez --------
    @AfterMapping
    default void wireParent(@MappingTarget FundoImobiliarioEntity parent) {
        List<FiiDividendoEntity> lst = parent.getFiiDividendos();
        if (lst != null) for (FiiDividendoEntity child : lst) child.setFundoImobiliario(parent);
    }

    // -------- Conversores (resolvem YearMonth ↔ LocalDate) --------
    default LocalDate map(YearMonth ym) { return ym == null ? null : ym.atDay(1); }
    default YearMonth map(LocalDate d)   { return d == null ? null : YearMonth.from(d); }
}
