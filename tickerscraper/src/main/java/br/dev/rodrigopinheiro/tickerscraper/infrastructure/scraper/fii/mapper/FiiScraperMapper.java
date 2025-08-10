package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.mapper;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.FiiDividendo;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.*;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FiiScraperMapper {

    @Mappings({
            // Header
            @Mapping(source = "infoHeader.ticker",    target = "ticker",      qualifiedByName = "upperTrim"),
            @Mapping(source = "infoHeader.nomeEmpresa", target = "nomeEmpresa"),

            // Info Sobre (strings)
            @Mapping(source = "infoSobre.razaoSocial",         target = "razaoSocial"),
            @Mapping(source = "infoSobre.cnpj",                target = "cnpj"),
            @Mapping(source = "infoSobre.publicoAlvo",         target = "publicoAlvo"),
            @Mapping(source = "infoSobre.mandato",             target = "mandato"),
            @Mapping(source = "infoSobre.segmento",            target = "segmento"),
            @Mapping(source = "infoSobre.tipoDeFundo",         target = "tipoDeFundo"),
            @Mapping(source = "infoSobre.prazoDeDuracao",      target = "prazoDeDuracao"),
            @Mapping(source = "infoSobre.tipoDeGestao",        target = "tipoDeGestao"),

            // Info Sobre (numéricos String → BigDecimal)
            @Mapping(source = "infoSobre.taxaDeAdministracao", target = "taxaDeAdministracao", qualifiedByName = "toBig"),
            @Mapping(source = "infoSobre.ultimoRendimento",    target = "ultimoRendimento",    qualifiedByName = "toBig"),
            @Mapping(source = "infoSobre.numeroDeCotistas",    target = "numeroDeCotistas",    qualifiedByName = "toBig"),
            @Mapping(source = "infoSobre.cotasEmitidas",       target = "cotasEmitidas",       qualifiedByName = "toBig"),
            @Mapping(source = "infoSobre.valorPatrimonial",    target = "valorPatrimonial",    qualifiedByName = "toBig"),
            @Mapping(source = "infoSobre.valorPatrimonialPorCota", target = "valorPatrimonialPorCota", qualifiedByName = "toBig"),
            @Mapping(source = "infoSobre.vacancia",            target = "vacancia",            qualifiedByName = "toBig"),

            // Cards (só cotação; resto não veio nos DTOs que você mandou)
            @Mapping(target = "cotacao", ignore = true),
            @Mapping(target = "valorDeMercado", ignore = true),
            @Mapping(target = "pvp", ignore = true),
            @Mapping(target = "dividendYield", ignore = true),
            @Mapping(target = "liquidezDiaria", ignore = true),

            // Dividendos entram no @AfterMapping
            @Mapping(target = "fiiDividendos", ignore = true)
    })
    FundoImobiliario toDomain(FiiDadosFinanceirosDTO dto);

    @AfterMapping
    default void fillDerived(FiiDadosFinanceirosDTO dto, @MappingTarget FundoImobiliario target) {
        // Dividendos
        List<FiiDividendoDTO> src = dto.dividendos();
        if (src != null && !src.isEmpty()) {
            target.setFiiDividendos(src.stream().map(this::toDomainDividendo).toList());
        }

        // Cotação: preferir FiiCotacaoDTO.preco; senão tentar cards.cotacao (String -> BigDecimal)
        if (dto.cotacao() != null && dto.cotacao().preco() != null) {
            target.setCotacao(dto.cotacao().preco());
        } else if (dto.infoCards() != null && dto.infoCards().cotacao() != null) {
            target.setCotacao(toBig(dto.infoCards().cotacao()));
        }
    }

    // Dividendos: seus DTOs têm (valorPago: BigDecimal) + (dataDeReferencia: YearMonth)
    @Mappings({
            @Mapping(target = "mes",   source = "dataDeReferencia"),
            @Mapping(target = "valor", source = "valorPago")
    })
    FiiDividendo toDomainDividendo(FiiDividendoDTO dto);

    // Helpers
    @Named("upperTrim")
    default String upperTrim(String s) {
        return s == null ? null : s.trim().toUpperCase();
    }

    @Named("toBig")
    default BigDecimal toBig(String raw) {
        if (raw == null) return null;
        String cleaned = raw.replace("R$", "").replace("%", "").replace(".", "").replace(",", ".").trim();
        if (cleaned.isBlank()) return null;
        try { return new BigDecimal(cleaned); } catch (Exception e) { return null; }
    }

    @Named("toYm")
    default YearMonth toYm(LocalDate d) { return d == null ? null : YearMonth.from(d); }
}
