package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.FiiDividendoResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.FiiResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FiiDividendo;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface FiiApiMapper {

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
            @Mapping(source = "dataAtualizacao",         target = "dataAtualizacao"),
            @Mapping(source = "fiiDividendos",           target = "dividendos")
    })
    FiiResponseDTO toResponse(FundoImobiliario domain);

    @Mappings({
            @Mapping(source = "mes",   target = "mes",   qualifiedByName = "ymToMMYYYY"),
            @Mapping(source = "valor", target = "valor")
    })
    FiiDividendoResponseDTO toResponse(FiiDividendo d);

    @Named("ymToMMYYYY")
    default String ymToMMYYYY(java.time.YearMonth ym) {
        return ym == null ? null : ym.format(DateTimeFormatter.ofPattern("MM/yyyy"));
    }
}
