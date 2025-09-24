package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.mapper;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.*;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Mapper responsável por converter o agregado de scraping de BDR ({@link BdrDadosFinanceirosDTO})
 * em objetos de domínio prontos para persistência.
 */
@Component
public class BdrScraperMapper {

    private static final Pattern YEAR_EXTRACTOR = Pattern.compile("(20\\d{2}|19\\d{2})");

    public Bdr toDomain(BdrDadosFinanceirosDTO raw) {
        if (raw == null) {
            return null;
        }

        Bdr bdr = new Bdr();
        bdr.setTicker(normalize(raw.ticker()));
        bdr.setInvestidorId(normalizeInvestidorId(raw.investidorId()));
        bdr.setTipoAtivo(TipoAtivo.BDR);

        // Informações de apresentação
        BdrHtmlMetadataDTO metadata = raw.htmlMetadata();
        if (metadata != null) {
            bdr.setNomeBdr(resolveCompanyName(metadata));
            bdr.setSetor(getMetaTag(metadata, "og:site_name"));
        }
        bdr.setPriceCurrency(resolveCurrency(raw));
        bdr.setFinancialsCurrency("USD");

        // Indicadores principais
        BdrIndicadoresDTO indicadores = raw.indicadores();
        BigDecimal precoAtual = findMonetario(indicadores, "PRECO ATUAL", "PREÇO ATUAL", "ULTIMO PRECO", "ÚLTIMO PREÇO", "PRICE");
        BigDecimal variacaoAno = findPercentual(indicadores, "VARIACAO ANO", "VAR. ANO", "12M", "ANO");

        bdr.setCotacao(precoAtual);
        bdr.setVariacao12(variacaoAno);

        // Indicadores correntes detalhados
        bdr.setCurrentIndicators(buildCurrentIndicators(indicadores));

        // Paridade
        bdr.setParidade(mapParidade(indicadores.paridade()));

        // Séries históricas e dividendos
        bdr.setPriceSeries(mapPriceSeries(raw.cotacoes()));
        bdr.setDividendYears(mapDividendos(raw.dividendos()));
        bdr.setHistoricalIndicators(mapHistoricalIndicators(indicadores));

        // Demonstrativos ainda não parseados – armazenados como listas vazias para futuras iterações
        bdr.setDreYears(List.of());
        bdr.setBpYears(List.of());
        bdr.setFcYears(List.of());

        // Metadados adicionais
        bdr.setUpdatedAt(Optional.ofNullable(raw.updatedAt()).orElseGet(Instant::now));
        bdr.setRawJson(raw.rawJson() == null ? Map.of() : new LinkedHashMap<>(raw.rawJson()));

        return bdr;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeInvestidorId(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String resolveCompanyName(BdrHtmlMetadataDTO metadata) {
        if (metadata == null) {
            return null;
        }
        Map<String, String> tags = metadata.metaTags();
        if (tags != null) {
            if (tags.containsKey("og:title")) {
                return tags.get("og:title");
            }
            if (tags.containsKey("twitter:title")) {
                return tags.get("twitter:title");
            }
        }
        return metadata.titulo();
    }

    private String getMetaTag(BdrHtmlMetadataDTO metadata, String key) {
        if (metadata == null || metadata.metaTags() == null) {
            return null;
        }
        return metadata.metaTags().get(key);
    }

    private String resolveCurrency(BdrDadosFinanceirosDTO raw) {
        if (raw.indicadores() != null && raw.indicadores().moedaPadrao() != null) {
            return raw.indicadores().moedaPadrao();
        }
        if (raw.cotacoes() != null) {
            return raw.cotacoes().moeda();
        }
        return null;
    }

    private BigDecimal findMonetario(BdrIndicadoresDTO indicadores, String... expectedNames) {
        if (indicadores == null || indicadores.indicadoresMonetarios() == null) {
            return null;
        }
        return findByNormalizedKey(indicadores.indicadoresMonetarios(), expectedNames);
    }

    private BigDecimal findPercentual(BdrIndicadoresDTO indicadores, String... expectedNames) {
        if (indicadores == null || indicadores.indicadoresPercentuais() == null) {
            return null;
        }
        return findByNormalizedKey(indicadores.indicadoresPercentuais(), expectedNames);
    }

    private BigDecimal findSimpleDecimal(BdrIndicadoresDTO indicadores, String... expectedNames) {
        if (indicadores == null || indicadores.indicadoresSimples() == null) {
            return null;
        }
        Map<String, Double> simples = indicadores.indicadoresSimples();
        for (String candidate : expectedNames) {
            String normalized = IndicadorParser.normalizar(candidate);
            Optional<Map.Entry<String, Double>> match = simples.entrySet().stream()
                    .filter(entry -> normalized.equalsIgnoreCase(IndicadorParser.normalizar(entry.getKey())))
                    .findFirst();
            if (match.isPresent() && match.get().getValue() != null) {
                return BigDecimal.valueOf(match.get().getValue());
            }
        }
        return null;
    }

    private BigDecimal findByNormalizedKey(Map<String, BigDecimal> source, String... expectedNames) {
        if (source.isEmpty()) {
            return null;
        }
        for (String candidate : expectedNames) {
            String normalized = IndicadorParser.normalizar(candidate);
            Optional<Map.Entry<String, BigDecimal>> match = source.entrySet().stream()
                    .filter(entry -> normalized.equalsIgnoreCase(IndicadorParser.normalizar(entry.getKey())))
                    .findFirst();
            if (match.isPresent()) {
                return match.get().getValue();
            }
        }
        return null;
    }

    private CurrentIndicators buildCurrentIndicators(BdrIndicadoresDTO indicadores) {
        if (indicadores == null) {
            return null;
        }
        CurrentIndicators current = new CurrentIndicators();
        current.setPl(findSimpleDecimal(indicadores, "P/L", "P L", "PRICE TO EARNINGS"));
        current.setPvp(findSimpleDecimal(indicadores, "P/VP", "P VP", "PRICE TO BOOK"));
        current.setPsr(findSimpleDecimal(indicadores, "P/S", "P SR", "PSR", "PRICE TO SALES"));
        current.setPEbit(findSimpleDecimal(indicadores, "P/EBIT", "P EBIT"));
        current.setRoe(findPercentual(indicadores, "ROE", "RETORNO SOBRE PATRIMONIO", "RETORNO SOBRE PATRIMÔNIO"));
        current.setMargens(buildCurrentMargins(indicadores));
        current.setVpa(findSimpleDecimal(indicadores, "VPA", "VALOR PATRIMONIAL POR AÇÃO"));
        current.setLpa(findSimpleDecimal(indicadores, "LPA", "LUCRO POR AÇÃO"));
        current.setPatrimonioPorAtivos(findSimpleDecimal(indicadores,
                "PATRIMONIO/ATIVOS",
                "PATRIMÔNIO/ATIVOS",
                "PATRIMONIO ATIVOS",
                "PATRIMONIO SOBRE ATIVOS"));
        return current;
    }

    private CurrentMargins buildCurrentMargins(BdrIndicadoresDTO indicadores) {
        BigDecimal margemBruta = findPercentual(indicadores, "MARGEM BRUTA", "GROSS MARGIN");
        BigDecimal margemEbit = findPercentual(indicadores, "MARGEM EBIT", "EBIT MARGIN");
        BigDecimal margemLiquida = findPercentual(indicadores, "MARGEM LIQUIDA", "MARGEM LÍQUIDA", "NET MARGIN");

        if (margemBruta == null && margemEbit == null && margemLiquida == null) {
            return null;
        }

        CurrentMargins margens = new CurrentMargins();
        margens.setMargemBruta(margemBruta);
        margens.setMargemEbit(margemEbit);
        margens.setMargemLiquida(margemLiquida);
        return margens;
    }

    private ParidadeBdr mapParidade(IndicadorParser.ParidadeBdrInfo info) {
        if (info == null) {
            return null;
        }
        ParidadeBdr paridade = new ParidadeBdr();
        paridade.setRatio(calculateRatio(info));
        paridade.setMethod(ParidadeMethod.SOURCE_HTML);
        paridade.setLastVerifiedAt(Instant.now());
        paridade.setRaw(info.descricaoOriginal());
        return paridade;
    }

    private Integer calculateRatio(IndicadorParser.ParidadeBdrInfo info) {
        if (info.quantidadeBdr() == null || info.quantidadeAcoes() == null) {
            return null;
        }
        if (info.quantidadeAcoes().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal ratio = info.quantidadeBdr().divide(info.quantidadeAcoes(), 8, RoundingMode.HALF_UP);
        BigDecimal normalized = ratio.stripTrailingZeros();
        if (normalized.scale() <= 0) {
            try {
                return normalized.intValueExact();
            } catch (ArithmeticException ignored) {
                return null;
            }
        }
        return null;
    }

    private List<PricePoint> mapPriceSeries(BdrCotacoesDTO cotacoes) {
        if (cotacoes == null || cotacoes.serie() == null) {
            return List.of();
        }
        return cotacoes.serie().stream()
                .filter(Objects::nonNull)
                .map(point -> {
                    PricePoint domain = new PricePoint();
                    domain.setDate(convertToLocalDate(point.data()));
                    domain.setClose(point.preco());
                    return domain;
                })
                .sorted(Comparator.comparing(PricePoint::getDate))
                .collect(Collectors.toList());
    }

    private LocalDate convertToLocalDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }

    private List<DividendYear> mapDividendos(BdrDividendosDTO dividendos) {
        if (dividendos == null || dividendos.dividendos() == null || dividendos.dividendos().isEmpty()) {
            return List.of();
        }
        Map<Integer, BigDecimal> acumuladoPorAno = new TreeMap<>();
        Map<Integer, String> currencyPorAno = new HashMap<>();
        for (BdrDividendoItemDTO item : dividendos.dividendos()) {
            if (item == null || item.valor() == null) {
                continue;
            }
            Integer ano = extractYear(item.periodo());
            if (ano == null) {
                continue;
            }
            acumuladoPorAno.merge(ano, item.valor(), BigDecimal::add);
            if (currencyPorAno.get(ano) == null && item.moeda() != null) {
                currencyPorAno.put(ano, item.moeda());
            }
        }
        return acumuladoPorAno.entrySet().stream()
                .map(entry -> {
                    DividendYear year = new DividendYear();
                    year.setAno(entry.getKey());
                    year.setTotalDividendo(entry.getValue().setScale(4, RoundingMode.HALF_UP));
                    year.setCurrency(currencyPorAno.get(entry.getKey()));
                    return year;
                })
                .collect(Collectors.toList());
    }

    private Integer extractYear(String periodo) {
        if (periodo == null) {
            return null;
        }
        Matcher matcher = YEAR_EXTRACTOR.matcher(periodo);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private List<HistoricalIndicator> mapHistoricalIndicators(BdrIndicadoresDTO indicadores) {
        if (indicadores == null || indicadores.raw() == null) {
            return List.of();
        }
        JsonNode historyNode = indicadores.raw().get("history");
        if (historyNode == null || !historyNode.isArray() || historyNode.isEmpty()) {
            return List.of();
        }
        List<HistoricalIndicator> history = new ArrayList<>();
        for (JsonNode item : historyNode) {
            if (item == null || !item.isObject()) {
                continue;
            }
            HistoricalIndicator indicator = new HistoricalIndicator();
            indicator.setYear(extractYear(item.get("year")));
            indicator.setPl(asBigDecimal(item.get("pl")));
            indicator.setPvp(asBigDecimal(item.get("pvp")));
            indicator.setPsr(asBigDecimal(item.get("psr")));
            indicator.setPEbit(asBigDecimal(item.get("pEbit")));
            indicator.setPEbitda(asBigDecimal(item.get("pEbitda")));
            indicator.setPAtivo(asBigDecimal(item.get("pAtivo")));
            indicator.setRoe(asBigDecimal(item.get("roe")));
            indicator.setRoic(asBigDecimal(item.get("roic")));
            indicator.setRoa(asBigDecimal(item.get("roa")));
            indicator.setMargemBruta(asBigDecimal(item.get("margemBruta")));
            indicator.setMargemOperacional(asBigDecimal(item.get("margemOperacional")));
            indicator.setMargemLiquida(asBigDecimal(item.get("margemLiquida")));
            indicator.setVpa(asBigDecimal(item.get("vpa")));
            indicator.setLpa(asBigDecimal(item.get("lpa")));
            indicator.setPatrimonioPorAtivos(asBigDecimal(item.get("patrimonioPorAtivos")));
            history.add(indicator);
        }
        return history;
    }

    private Integer extractYear(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isInt()) {
            return node.intValue();
        }
        if (node.isTextual()) {
            String text = node.asText();
            if (text == null || text.isBlank()) {
                return null;
            }
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return IndicadorParser.safeParseDouble(text)
                        .map(Double::intValue)
                        .orElse(null);
            }
        }
        if (node.canConvertToInt()) {
            return node.intValue();
        }
        return null;
    }

    private BigDecimal asBigDecimal(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isTextual()) {
            return IndicadorParser.safeParseDouble(node.asText())
                    .map(BigDecimal::valueOf)
                    .orElse(null);
        }
        return null;
    }
}
