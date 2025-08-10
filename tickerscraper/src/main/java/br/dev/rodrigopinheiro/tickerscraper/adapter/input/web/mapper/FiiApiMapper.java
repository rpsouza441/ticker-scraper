package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.FiiDividendoResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.FiiResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FiiDividendoEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FiiApiMapper {

    // ===== Entity -> API DTO =====
    @Mappings({
            // Campos 1:1
            @Mapping(source = "ticker",                   target = "ticker"),
            @Mapping(source = "nomeEmpresa",              target = "nomeEmpresa"),
            @Mapping(source = "razaoSocial",              target = "razaoSocial"),
            @Mapping(source = "cnpj",                     target = "cnpj"),
            @Mapping(source = "publicoAlvo",              target = "publicoAlvo"),
            @Mapping(source = "mandato",                  target = "mandato"),
            @Mapping(source = "segmento",                 target = "segmento"),
            @Mapping(source = "tipoDeFundo",              target = "tipoDeFundo"),
            @Mapping(source = "prazoDeDuracao",           target = "prazoDeDuracao"),
            @Mapping(source = "tipoDeGestao",             target = "tipoDeGestao"),
            @Mapping(source = "taxaDeAdministracao",      target = "taxaDeAdministracao"),
            @Mapping(source = "ultimoRendimento",         target = "ultimoRendimento"),
            @Mapping(source = "cotacao",                  target = "cotacao"),
            @Mapping(source = "valorDeMercado",           target = "valorDeMercado"),
            @Mapping(source = "pvp",                      target = "pvp"),
            @Mapping(source = "dividendYield",            target = "dividendYield"),
            @Mapping(source = "liquidezDiaria",           target = "liquidezDiaria"),
            @Mapping(source = "valorPatrimonial",         target = "valorPatrimonial"),
            @Mapping(source = "valorPatrimonialPorCota",  target = "valorPatrimonialPorCota"),
            @Mapping(source = "vacancia",                 target = "vacancia"),
            @Mapping(source = "numeroDeCotistas",         target = "numeroDeCotistas"),
            @Mapping(source = "cotasEmitidas",            target = "cotasEmitidas"),

            // Metadados
            @Mapping(source = "dataAtualizacao",          target = "dataAtualizacao"),

            // Coleção
            @Mapping(source = "fiiDividendos",            target = "dividendos")
    })
    FiiResponseDTO toResponseDto(FundoImobiliarioEntity entity);

    @Mappings({
            // mes (LocalDate do dia 1) -> "YYYY-MM"
            @Mapping(source = "mes",   target = "mes",   qualifiedByName = "localDateToYmString"),
            @Mapping(source = "valor", target = "valor")
    })
    FiiDividendoResponseDTO toDividendoResponse(FiiDividendoEntity entity);

    List<FiiDividendoResponseDTO> toDividendoResponseList(List<FiiDividendoEntity> list);

    // ===== Helpers =====
    @Named("localDateToYmString")
    default String localDateToYmString(LocalDate date) {
        if (date == null) return null;
        YearMonth ym = YearMonth.from(date);
        return ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    // (Opcional) formatação padrão monetária/percentual na camada API, se quiser:
    default String formatBig(BigDecimal v) {
        return v == null ? null : v.toPlainString();
    }
}
