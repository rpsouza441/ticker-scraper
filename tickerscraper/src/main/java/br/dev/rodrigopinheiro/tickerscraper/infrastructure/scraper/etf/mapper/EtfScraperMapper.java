package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.Etf;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.dto.EtfDadosFinanceirosDTO;

/**
 * Mapper para conversão entre DTOs de infraestrutura e entidade de domínio ETF.
 * Utiliza MapStruct para mapeamento automático e type-safe.
 * Usa IndicadorParser para conversões numéricas robustas.
 */
@Mapper(componentModel = "spring", uses = IndicadorParser.class)
public interface EtfScraperMapper {

    /**
     * Converte DTO de infraestrutura para entidade de domínio.
     * 
     * @param dto DTO com dados financeiros do ETF
     * @return Entidade de domínio Etf
     */
    @Mapping(source = "infoHeader.ticker", target = "ticker")
    @Mapping(source = "infoHeader.nomeEtf", target = "nomeEtf")
    @Mapping(source = "infoCards.valorAtual", target = "valorAtual", qualifiedByName = "paraBigDecimal")
    @Mapping(source = "infoCards.capitalizacao", target = "capitalizacao", qualifiedByName = "paraBigDecimal")
    @Mapping(source = "infoCards.variacao12M", target = "variacao12M", qualifiedByName = "paraBigDecimal")
    @Mapping(source = "infoCards.variacao60M", target = "variacao60M", qualifiedByName = "paraBigDecimal")
    @Mapping(source = "infoCards.dy", target = "dy", qualifiedByName = "paraBigDecimal")
    @Mapping(target = "tipoAtivo", constant = "ETF")
    @Mapping(target = "dataAtualizacao", expression = "java(java.time.LocalDateTime.now())")
    Etf toDomain(EtfDadosFinanceirosDTO dto);
}