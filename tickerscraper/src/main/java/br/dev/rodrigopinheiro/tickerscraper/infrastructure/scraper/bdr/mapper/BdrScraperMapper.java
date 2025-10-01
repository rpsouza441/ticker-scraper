package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.mapper;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Dividendo;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoDividendo;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrDadosFinanceirosDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {IndicadorParser.class})
public interface BdrScraperMapper {

    @Mappings({
            // === Mapeamentos de HTML (Header, Cards, Sobre) ===
            // Removido o prefixo "dto." desnecessário
            @Mapping(source = "infoHeader.ticker", target = "ticker", qualifiedByName = "limpezaComUpperCase"),
            @Mapping(source = "infoHeader.nomeBdr", target = "nome", qualifiedByName = "normalizar"),
            @Mapping(target = "precoAtual", expression = "java(getPrecoAtual(dto))"),
            @Mapping(target = "variacao12M", source = "infoCards.variacao12M"),
            @Mapping(target = "setor", expression = "java(getSetor(dto))"),
            @Mapping(target = "industria", expression = "java(getIndustria(dto))"),
            @Mapping(source = "infoSobre.marketCapText", target = "marketCapValue", qualifiedByName = "paraBigDecimal"),
            @Mapping(target = "marketCapCurrency", expression = "java(extractCurrency(dto.infoSobre().marketCapText()))"),
            @Mapping(target = "paridadeRatio", expression = "java(extractParityRatio(dto.infoSobre().paridadeText()))"),

            // === Mapeamentos de API (Indicadores, Demonstrativos, Dividendos) ===
            // Indicadores (extraindo o valor 'Atual')
            @Mapping(target = "pl", expression = "java(getIndicatorValueAsDecimal(dto.indicadores(), \"P/L\"))"),
            @Mapping(target = "pvp", expression = "java(getIndicatorValueAsDecimal(dto.indicadores(), \"P/VP\"))"),
            @Mapping(target = "psr", expression = "java(getIndicatorValueAsDecimal(dto.indicadores(), \"P/RECEITA (PSR)\"))"),
            @Mapping(target = "pEbit", expression = "java(getIndicatorValueAsDecimal(dto.indicadores(), \"P/EBIT\"))"),
            @Mapping(target = "pEbitda", expression = "java(getIndicatorValueAsDecimal(dto.indicadores(), \"P/EBITDA\"))"),
            @Mapping(target = "pAtivo", expression = "java(getIndicatorValueAsDecimal(dto.indicadores(), \"P/ATIVO\"))"),
            @Mapping(target = "roe", expression = "java(getIndicatorValueAsPercent(dto.indicadores(), \"ROE\"))"),
            @Mapping(target = "roic", expression = "java(getIndicatorValueAsPercent(dto.indicadores(), \"ROIC\"))"),
            @Mapping(target = "roa", expression = "java(getIndicatorValueAsPercent(dto.indicadores(), \"ROA\"))"),
            @Mapping(target = "margemBruta", expression = "java(getIndicatorValueAsPercent(dto.indicadores(), \"MARGEM BRUTA\"))"),
            @Mapping(target = "margemOperacional", expression = "java(getIndicatorValueAsPercent(dto.indicadores(), \"MARGEM OPERACIONAL\"))"),
            @Mapping(target = "margemLiquida", expression = "java(getIndicatorValueAsPercent(dto.indicadores(), \"MARGEM LÍQUIDA\"))"),
            @Mapping(target = "vpa", expression = "java(getIndicatorValueAsDecimal(dto.indicadores(), \"VPA\"))"),
            @Mapping(target = "lpa", expression = "java(getIndicatorValueAsDecimal(dto.indicadores(), \"LPA\"))"),
            @Mapping(target = "dividendYield", expression = "java(getIndicatorValueAsPercent(dto.indicadores(), \"DIVIDEND YIELD (DY)\"))"),

            // DRE (extraindo o valor de 'ÚLT. 12M')
            @Mapping(target = "receitaTotalUsd", expression = "java(getFinancialStatementValue(dto.demonstrativos().dre(), \"Receita Total - (US$)\"))"),
            @Mapping(target = "lucroBrutoUsd", expression = "java(getFinancialStatementValue(dto.demonstrativos().dre(), \"Lucro Bruto - (US$)\"))"),
            @Mapping(target = "ebitdaUsd", expression = "java(getFinancialStatementValue(dto.demonstrativos().dre(), \"EBITDA\"))"),
            @Mapping(target = "ebitUsd", expression = "java(getFinancialStatementValue(dto.demonstrativos().dre(), \"EBIT\"))"),
            @Mapping(target = "lucroLiquidoUsd", expression = "java(getFinancialStatementValue(dto.demonstrativos().dre(), \"Lucro Líquido - (US$)\"))"),

            // Balanço Patrimonial
            @Mapping(target = "ativosTotaisUsd", expression = "java(getFinancialStatementValue(dto.demonstrativos().bp(), \"Ativos Total - ($)\"))"),
            @Mapping(target = "passivosTotaisUsd", expression = "java(getFinancialStatementValue(dto.demonstrativos().bp(), \"Passivos Total - (US$)\"))"),
            @Mapping(target = "dividaLpUsd", expression = "java(getFinancialStatementValue(dto.demonstrativos().bp(), \"Dívida a Longo Prazo - ($)\"))"),
            @Mapping(target = "plUsd", expression = "java(getFinancialStatementValue(dto.demonstrativos().bp(), \"Patrimônio Líquido - ($)\"))"),

            // Fluxo de Caixa
            @Mapping(target = "fcoUsd", expression = "java(getFinancialStatementValue(dto.demonstrativos().fc(), \"Fluxo de Caixa Operacional - ($)\"))"),
            @Mapping(target = "fciUsd", expression = "java(getFinancialStatementValue(dto.demonstrativos().fc(), \"Caixa Líquido usado para atividades de investimento - ($)\"))"),
            @Mapping(target = "fcfUsd", expression = "java(getFinancialStatementValue(dto.demonstrativos().fc(), \"Fluxo de Caixa Livre - ($)\"))"),

            // Campos constantes e ignorados
            @Mapping(target = "tipoAtivo", constant = "BDR"),
            @Mapping(target = "priceCurrency", constant = "BRL"),
            @Mapping(target = "financialsCurrency", constant = "USD"),
            @Mapping(source = "updatedAt", target = "dataAtualizacao"),
            @Mapping(target = "investidorId", ignore = true),
            @Mapping(target = "paridadeLastVerifiedAt", ignore = true),
            @Mapping(target = "dreYear", ignore = true),
            @Mapping(target = "bpYear", ignore = true),
            @Mapping(target = "fcYear", ignore = true),
            @Mapping(target = "dividendos", ignore = true)  // Ignorar dividendos no mapeamento principal
    })
    Bdr toDomain(BdrDadosFinanceirosDTO dto);

    @AfterMapping
    default void configureDividendos(@MappingTarget Bdr bdr, BdrDadosFinanceirosDTO dto) {
        // Configurar dividendos usando o método replaceDividendos para evitar UnsupportedOperationException
        List<Dividendo> dividendos = mapDividendos(dto.dividendos());
        if (dividendos != null && !dividendos.isEmpty()) {
            bdr.replaceDividendos(dividendos);
        }
    }

    // Método wrapper com logs de debug
    default Bdr toDomainWithDebug(BdrDadosFinanceirosDTO dto) {
        try {
            System.out.println("DEBUG: Iniciando mapeamento BDR - ticker: " + 
                (dto != null && dto.infoHeader() != null ? dto.infoHeader().ticker() : "null"));
            
            if (dto == null) {
                System.out.println("DEBUG: DTO é null");
                return null;
            }
            
            System.out.println("DEBUG: Chamando toDomain...");
            Bdr result = toDomain(dto);
            
            System.out.println("DEBUG: Mapeamento concluído com sucesso");
            return result;
            
        } catch (UnsupportedOperationException e) {
            System.out.println("DEBUG: UnsupportedOperationException capturada no mapeamento: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.out.println("DEBUG: Exceção capturada no mapeamento: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // =========================================================
    // MÉTODOS HELPER
    // =========================================================

    // Método para valores decimais (P/L, P/VP, VPA, LPA, etc.)
    default BigDecimal getIndicatorValueAsDecimal(Map<String, Object> indicators, String key) {
        if (indicators == null || !indicators.containsKey(key) || !(indicators.get(key) instanceof List)) {
            return null;
        }
        List<?> values = (List<?>) indicators.get(key);
        return values.stream()
                .filter(item -> item instanceof Map)
                .map(item -> (Map<?, ?>) item)
                .filter(map -> "Atual".equalsIgnoreCase(String.valueOf(map.get("year"))))
                .findFirst()
                .map(map -> {
                    String value = String.valueOf(map.get("value"));
                    return IndicadorParser.parseBigdecimal(value);
                })
                .orElse(null);
    }

    // Método para valores percentuais (ROE, ROA, ROIC, margens, DY)
    default BigDecimal getIndicatorValueAsPercent(Map<String, Object> indicators, String key) {
        if (indicators == null || !indicators.containsKey(key) || !(indicators.get(key) instanceof List)) {
            return null;
        }
        List<?> values = (List<?>) indicators.get(key);
        return values.stream()
                .filter(item -> item instanceof Map)
                .map(item -> (Map<?, ?>) item)
                .filter(map -> "Atual".equalsIgnoreCase(String.valueOf(map.get("year"))))
                .findFirst()
                .map(map -> {
                    String value = String.valueOf(map.get("value"));
                    // Para percentuais, o valor já vem como decimal (ex: "91.87" para 91.87%)
                    // Não precisamos multiplicar por 100
                    return IndicadorParser.parseBigdecimal(value);
                })
                .orElse(null);
    }

    // Método para obter preço atual (cotação)
    default BigDecimal getPrecoAtual(BdrDadosFinanceirosDTO dto) {
        if (dto == null || dto.infoCards() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal cotacao = dto.infoCards().cotacao();
        return cotacao != null ? cotacao : BigDecimal.ZERO;
    }

    // Método para obter setor corretamente
    default String getSetor(BdrDadosFinanceirosDTO dto) {
        if (dto == null || dto.infoSobre() == null) {
            return null;
        }
        return dto.infoSobre().setor();
    }

    // Método para obter indústria corretamente
    default String getIndustria(BdrDadosFinanceirosDTO dto) {
        if (dto == null || dto.infoSobre() == null) {
            return null;
        }
        return dto.infoSobre().industria();
    }

    default BigDecimal getFinancialStatementValue(Map<String, Object> statementData, String rowLabel) {
        final String period = "ÚLT. 12M";
        if (statementData == null || !statementData.containsKey(rowLabel) || !(statementData.get(rowLabel) instanceof Map)) {
            return null;
        }
        Map<?, ?> valuesByPeriod = (Map<?, ?>) statementData.get(rowLabel);
        if (valuesByPeriod == null || !valuesByPeriod.containsKey(period)) {
            return null;
        }
        String rawValue = String.valueOf(valuesByPeriod.get(period));
        return IndicadorParser.parseBigdecimal(rawValue);
    }

    @SuppressWarnings("unchecked")
    default List<Dividendo> mapDividendos(Map<String, Object> dividendosData) {
        if (dividendosData == null || !dividendosData.containsKey("content")) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> dividendosList = (List<Map<String, Object>>) dividendosData.get("content");
        // Usando ArrayList explicitamente para garantir que a lista seja mutável
        return dividendosList.stream()
                .map(div -> new Dividendo(
                        YearMonth.of((Integer) div.get("created_at"), 1),
                        IndicadorParser.parseBigdecimal(String.valueOf(div.get("price"))),
                        TipoDividendo.DIVIDENDO,
                        "USD"
                ))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    default String extractCurrency(String text) {
        return IndicadorParser.extrairMoeda(text).orElse("BRL");
    }

    default BigDecimal extractParityRatio(String text) {
        return IndicadorParser.parseParidadeBdr(text)
                .map(IndicadorParser.ParidadeBdrInfo::fatorConversao)
                .orElse(null);
    }
}