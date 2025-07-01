package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.DadosFinanceiros;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.IndicadorFundamentalista;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses={IndicadorParser.class})
public interface AcaoScraperMapper {

    @Mappings({
            //Mapeando InfoHeader
            @Mapping(source = "infoHeader.ticker", target = "ticker"),
            @Mapping(source = "infoHeader.nomeEmpresa", target ="nomeEmpresa"),

            //Mapeando InfoCards
            @Mapping(source = "infoCards.cotacao", target="precoAtual"),
            @Mapping(source = "infoCards.variacao12M", target = "variacao12M"),

            //Mapeando infoDetailed
            @Mapping(source = "infoDetailed.valorMercado", target = "valorMercado"),
            @Mapping(source = "infoDetailed.valorFirma", target = "valorFirma"),
            @Mapping(source = "infoDetailed.patrimonioLiquido", target = "patrimonioLiquido"),
            @Mapping(source = "infoDetailed.numeroTotalPapeis", target = "numeroTotalPapeis"),
            @Mapping(source = "infoDetailed.ativos", target = "ativos"),
            @Mapping(source = "infoDetailed.ativoCirculante", target = "ativoCirculantes"),
            @Mapping(source = "infoDetailed.dividaBruta", target = "dividaBruta"),
            @Mapping(source = "infoDetailed.dividaLiquida", target = "dividaLiquida"),
            @Mapping(source = "infoDetailed.segmentoListagem", target = "segmentoListagem"),
            @Mapping(source = "infoDetailed.freeFloat", target = "freeFloat"),
            @Mapping(source = "infoDetailed.tagAlong", target = "tagAlong"),
            @Mapping(source = "infoDetailed.liquidezMediaDiaria", target = "liquidezMediaDiaria"),
            @Mapping(source = "infoDetailed.setor", target = "setor"),
            @Mapping(source = "infoDetailed.segmento", target = "segmento"),

            //Mapeando IndicadoresFuncamentalistas
            // Indicadores principais
            @Mapping(target = "pL", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/L\"))"),
            @Mapping(target = "psr", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/RECEITA (PSR)\"))"),
            @Mapping(target = "pVp", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/VP\"))"),
            @Mapping(target = "dividendYeld", expression = "java(getIndicatorValueAsBigDecimal(dados, \"DIVIDEND YIELD\"))"),
            @Mapping(target = "payout", expression = "java(getIndicatorValueAsBigDecimal(dados, \"PAYOUT\"))"),
            @Mapping(target = "margemLiquida", expression = "java(getIndicatorValueAsBigDecimal(dados, \"MARGEM LÍQUIDA\"))"),
            @Mapping(target = "margemBruta", expression = "java(getIndicatorValueAsBigDecimal(dados, \"MARGEM BRUTA\"))"),
            @Mapping(target = "margemEbit", expression = "java(getIndicatorValueAsBigDecimal(dados, \"MARGEM EBIT\"))"),
            @Mapping(target = "margemEbitda", expression = "java(getIndicatorValueAsBigDecimal(dados, \"MARGEM EBITDA\"))"),
            @Mapping(target = "evEbitda", expression = "java(getIndicatorValueAsBigDecimal(dados, \"EV/EBITDA\"))"),
            @Mapping(target = "evEbit", expression = "java(getIndicatorValueAsBigDecimal(dados, \"EV/EBIT\"))"),
            @Mapping(target = "pEbitda", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/EBITDA\"))"),
            @Mapping(target = "pAtivo", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/ATIVO\"))"),
            @Mapping(target = "pCapitaldeGiro", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/CAP.GIRO\"))"),
            @Mapping(target = "pAtivoCirculanteLiquido", expression = "java(getIndicatorValueAsBigDecimal(dados, \"P/ATIVO CIRC LIQ\"))"),
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
    Acao toDomain(DadosFinanceiros dados);

    /**
     * RESPONSABILIDADE DO MAPPER: Navegar na estrutura do DadosFinanceiros para ACHAR o valor.
     */
    default BigDecimal getIndicatorValueAsBigDecimal(DadosFinanceiros dados, String nomeIndicador) {
        if (dados == null || dados.fundamentalIndicators() == null || dados.fundamentalIndicators().indicadores() == null) {
            return BigDecimal.ZERO;
        }
        IndicadorFundamentalista indicador = dados.fundamentalIndicators().indicadores().get(nomeIndicador);
        if (indicador == null || indicador.valor() == null) {
            return BigDecimal.ZERO;
        }

        // Entrega o texto bruto para o especialista em conversão
        return IndicadorParser.parseBigdecimal(indicador.valor());
    }


}
