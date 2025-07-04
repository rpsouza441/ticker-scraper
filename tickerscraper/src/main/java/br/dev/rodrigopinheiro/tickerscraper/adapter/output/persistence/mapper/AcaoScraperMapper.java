package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.AcaoDadosFinanceiros;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.AcaoIndicadorFundamentalista;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses={IndicadorParser.class})
public interface AcaoScraperMapper {
    Logger logger = LoggerFactory.getLogger(AcaoScraperMapper.class);


    @Mappings({
            //Mapeando InfoHeader
            @Mapping(source = "infoHeader.ticker", target = "ticker", qualifiedByName = "limpezaComUpperCase"),
            @Mapping(source = "infoHeader.nomeEmpresa", target ="nomeEmpresa"),

            //Mapeando InfoCards
            @Mapping(source = "infoCards.cotacao", target = "precoAtual", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoCards.variacao12M", target = "variacao12M", qualifiedByName = "paraBigDecimal"),

            //Mapeando infoDetailed
            @Mapping(source = "infoDetailed.valorMercado", target = "valorMercado", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoDetailed.valorFirma", target = "valorFirma", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoDetailed.patrimonioLiquido", target = "patrimonioLiquido", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoDetailed.numeroTotalPapeis", target = "numeroTotalPapeis", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoDetailed.ativos", target = "ativos", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoDetailed.ativoCirculante", target = "ativoCirculantes", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoDetailed.dividaBruta", target = "dividaBruta", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoDetailed.dividaLiquida", target = "dividaLiquida", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoDetailed.segmentoListagem", target = "segmentoListagem"),
            @Mapping(source = "infoDetailed.freeFloat", target = "freeFloat", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoDetailed.tagAlong", target = "tagAlong", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoDetailed.liquidezMediaDiaria", target = "liquidezMediaDiaria", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoDetailed.setor", target = "setor"),
            @Mapping(source = "infoDetailed.segmento", target = "segmento"),
            @Mapping(source = "infoDetailed.disponibilidade", target = "disponibilidade", qualifiedByName = "limparTextoIndicador"),


            //Mapeando IndicadoresFuncamentalistas
            // Indicadores principais
            @Mapping(target = "pl", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/L\"))"),
            @Mapping(target = "psr", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/RECEITA (PSR)\"))"),
            @Mapping(target = "pvp", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/VP\"))"),
            @Mapping(target = "dividendYield", expression = "java(getIndicatorValueAsBigDecimal(dados, \"DIVIDEND YIELD\"))"),
            @Mapping(target = "payout", expression = "java(getIndicatorValueAsBigDecimal(dados, \"PAYOUT\"))"),
            @Mapping(target = "margemLiquida", expression = "java(getIndicatorValueAsBigDecimal(dados, \"MARGEM LÍQUIDA\"))"),
            @Mapping(target = "margemBruta", expression = "java(getIndicatorValueAsBigDecimal(dados, \"MARGEM BRUTA\"))"),
            @Mapping(target = "margemEbit", expression = "java(getIndicatorValueAsBigDecimal(dados, \"MARGEM EBIT\"))"),
            @Mapping(target = "margemEbitda", expression = "java(getIndicatorValueAsBigDecimal(dados, \"MARGEM EBITDA\"))"),
            @Mapping(target = "evEbitda", expression = "java(getIndicatorValueAsBigDecimal(dados, \"EV/EBITDA\"))"),
            @Mapping(target = "evEbit", expression = "java(getIndicatorValueAsBigDecimal(dados, \"EV/EBIT\"))"),
            @Mapping(target = "pebitda", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/EBITDA\"))"),
            @Mapping(target = "pativo", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/ATIVO\"))"),
            @Mapping(target = "pcapitaldeGiro", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/CAP.GIRO\"))"),
            @Mapping(target = "pativoCirculanteLiquido", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/ATIVO CIRC LIQ\"))"),
            @Mapping(target = "vpa", expression = "java(getIndicatorValueAsBigDecimal(dados, \"VPA\"))"),
            @Mapping(target = "lpa", expression = "java(getIndicatorValueAsBigDecimal(dados, \"LPA\"))"),
            @Mapping(target = "giroAtivos", expression = "java(getIndicatorValueAsBigDecimal(dados, \"GIRO ATIVOS\"))"),
            @Mapping(target = "roe", expression = "java(getIndicatorValueAsBigDecimal(dados, \"ROE\"))"),
            @Mapping(target = "roic", expression = "java(getIndicatorValueAsBigDecimal(dados, \"ROIC\"))"),
            @Mapping(target = "roa", expression = "java(getIndicatorValueAsBigDecimal(dados, \"ROA\"))"),
            @Mapping(target = "dividaLiquidaPatrimonio", expression = "java(getIndicatorValueAsBigDecimal(dados, \"DÍVIDA LÍQUIDA / PATRIMÔNIO\"))"),
            @Mapping(target = "dividaLiquidaEbitda", expression = "java(getIndicatorValueAsBigDecimal(dados, \"DÍVIDA LÍQUIDA / EBITDA\"))"),
            @Mapping(target = "dividaLiquidaEbit", expression = "java(getIndicatorValueAsBigDecimal(dados, \"DÍVIDA LÍQUIDA / EBIT\"))"),
            @Mapping(target = "dividaBrutaPatrimonio", expression = "java(getIndicatorValueAsBigDecimal(dados, \"DÍVIDA BRUTA / PATRIMÔNIO\"))"),
            @Mapping(target = "patrimonioAtivos", expression = "java(getIndicatorValueAsBigDecimal(dados, \"PATRIMÔNIO / ATIVOS\"))"),
            @Mapping(target = "passivosAtivos", expression = "java(getIndicatorValueAsBigDecimal(dados, \"PASSIVOS / ATIVOS\"))"),
            @Mapping(target = "liquidezCorrente", expression = "java(getIndicatorValueAsBigDecimal(dados, \"LIQUIDEZ CORRENTE\"))"),
            @Mapping(target = "cagrReceitasCincoAnos", expression = "java(getIndicatorValueAsBigDecimal(dados, \"CAGR RECEITAS 5 ANOS\"))"),
            @Mapping(target = "cagrLucrosCincoAnos", expression = "java(getIndicatorValueAsBigDecimal(dados, \"CAGR LUCROS 5 ANOS\"))")

    })
    Acao toDomain(AcaoDadosFinanceiros dados);


    default BigDecimal getIndicatorValueAsBigDecimal(AcaoDadosFinanceiros dados, String nomeIndicador) {
        if (dados == null || dados.fundamentalIndicators() == null || dados.fundamentalIndicators().indicadores() == null) {
            return BigDecimal.ZERO;
        }
        AcaoIndicadorFundamentalista indicador = dados.fundamentalIndicators().indicadores().get(nomeIndicador);

        // VERIFICAÇÃO E LOG
        if (indicador == null) {
            logger.warn("Indicador com a chave '{}' não foi encontrado nos dados raspados.", nomeIndicador);
            return BigDecimal.ZERO;
        }
        if (indicador.valor() == null) {
            logger.warn("Indicador '{}' foi encontrado, mas seu valor é nulo.", nomeIndicador);
            return BigDecimal.ZERO;
        }

        return IndicadorParser.parseBigdecimal(indicador.valor());
    }


}
