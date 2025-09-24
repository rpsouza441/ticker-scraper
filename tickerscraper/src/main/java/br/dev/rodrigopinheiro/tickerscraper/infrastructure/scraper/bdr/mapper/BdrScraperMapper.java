package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.mapper;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.*;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.Quality;
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
import java.util.concurrent.atomic.AtomicReference;

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
        }

        BdrIndicadoresDTO indicadores = raw.indicadores();
        bdr.setSetor(resolveSetor(indicadores, metadata));
        bdr.setIndustria(resolveIndustria(indicadores, metadata));

        bdr.setPriceCurrency(resolveCurrency(raw));

        // Indicadores principais
        BigDecimal precoAtual = findMonetario(indicadores, "PRECO ATUAL", "PREÇO ATUAL", "ULTIMO PRECO", "ÚLTIMO PREÇO", "PRICE");
        BigDecimal variacaoAno = findPercentual(indicadores, "VARIACAO ANO", "VAR. ANO", "12M", "ANO");

        bdr.setCotacao(precoAtual);
        bdr.setVariacao12(variacaoAno);

        // Indicadores correntes detalhados
        bdr.setCurrentIndicators(buildCurrentIndicators(indicadores));

        // Market cap estruturado
        bdr.setMarketCap(mapMarketCap(indicadores));

        // Paridade
        bdr.setParidade(mapParidade(indicadores.paridade()));

        // Séries históricas e dividendos
        bdr.setPriceSeries(mapPriceSeries(raw.cotacoes()));
        bdr.setDividendYears(mapDividendos(raw.dividendos()));
        bdr.setHistoricalIndicators(mapHistoricalIndicators(indicadores));

        // Séries anuais estruturadas
        AtomicReference<String> financialCurrency = new AtomicReference<>();
        bdr.setDreYears(mapDreYears(raw.dre(), financialCurrency));
        bdr.setBpYears(mapBpYears(raw.balancoPatrimonial(), financialCurrency));
        bdr.setFcYears(mapFcYears(raw.fluxoDeCaixa(), financialCurrency));

        bdr.setFinancialsCurrency(resolveFinancialCurrency(financialCurrency.get(), indicadores, bdr.getPriceCurrency()));

        // Metadados adicionais
        bdr.setUpdatedAt(Optional.ofNullable(raw.updatedAt()).orElseGet(Instant::now));

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

    private String resolveSetor(BdrIndicadoresDTO indicadores, BdrHtmlMetadataDTO metadata) {
        String fromApi = extractIndicatorText(indicadores, "SETOR", "SECTOR");
        if (fromApi != null && !fromApi.isBlank()) {
            return fromApi;
        }
        String fromMeta = extractMetaByKeyword(metadata, "setor", "sector");
        if (fromMeta != null && !fromMeta.isBlank()) {
            return fromMeta;
        }
        return parseDescriptionForLabel(metadata, "Setor");
    }

    private String resolveIndustria(BdrIndicadoresDTO indicadores, BdrHtmlMetadataDTO metadata) {
        String fromApi = extractIndicatorText(indicadores, "INDUSTRIA", "INDUSTRY");
        if (fromApi != null && !fromApi.isBlank()) {
            return fromApi;
        }
        String fromMeta = extractMetaByKeyword(metadata, "industria", "industry");
        if (fromMeta != null && !fromMeta.isBlank()) {
            return fromMeta;
        }
        return parseDescriptionForLabel(metadata, "Indústria");
    }

    private String extractIndicatorText(BdrIndicadoresDTO indicadores, String... expectedNames) {
        if (indicadores == null || indicadores.raw() == null) {
            return null;
        }
        return findIndicatorNode(indicadores.raw(), expectedNames)
                .map(this::extractIndicatorRawValue)
                .map(value -> value == null ? null : value.trim())
                .orElse(null);
    }

    private Optional<JsonNode> findIndicatorNode(JsonNode root, String... expectedNames) {
        if (root == null || root.isNull()) {
            return Optional.empty();
        }
        Set<String> expected = Arrays.stream(expectedNames)
                .filter(Objects::nonNull)
                .map(IndicadorParser::normalizar)
                .collect(Collectors.toSet());
        if (expected.isEmpty()) {
            return Optional.empty();
        }
        return findIndicatorNodeRecursive(root, expected);
    }

    private Optional<JsonNode> findIndicatorNodeRecursive(JsonNode node, Set<String> expectedNames) {
        if (node == null || node.isNull()) {
            return Optional.empty();
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                Optional<JsonNode> candidate = findIndicatorNodeRecursive(item, expectedNames);
                if (candidate.isPresent()) {
                    return candidate;
                }
            }
            return Optional.empty();
        }
        if (node.isObject()) {
            String name = extractIndicatorName(node);
            if (name != null && expectedNames.contains(IndicadorParser.normalizar(name))) {
                return Optional.of(node);
            }
            Iterator<JsonNode> it = node.elements();
            while (it.hasNext()) {
                Optional<JsonNode> candidate = findIndicatorNodeRecursive(it.next(), expectedNames);
                if (candidate.isPresent()) {
                    return candidate;
                }
            }
        }
        return Optional.empty();
    }

    private String extractIndicatorName(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String field : List.of("nome", "label", "name", "title", "descricao", "description")) {
            JsonNode value = node.get(field);
            if (value != null && value.isTextual()) {
                return value.asText();
            }
        }
        return null;
    }

    private String extractIndicatorRawValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        for (String field : List.of("valor", "value", "texto", "text", "raw", "formatted")) {
            JsonNode value = node.get(field);
            if (value != null && value.isTextual()) {
                return value.asText();
            }
            if (value != null && value.isNumber()) {
                return value.asText();
            }
        }
        if (node.isNumber()) {
            return node.asText();
        }
        return null;
    }

    private String extractMetaByKeyword(BdrHtmlMetadataDTO metadata, String... keywords) {
        if (metadata == null || metadata.metaTags() == null || metadata.metaTags().isEmpty()) {
            return null;
        }
        Map<String, String> tags = metadata.metaTags();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            String lower = key.toLowerCase(Locale.ROOT);
            for (String keyword : keywords) {
                if (keyword != null && lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private String parseDescriptionForLabel(BdrHtmlMetadataDTO metadata, String label) {
        if (metadata == null) {
            return null;
        }
        String description = metadata.descricao();
        if (description == null || description.isBlank() || label == null || label.isBlank()) {
            return null;
        }
        Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(label) + "\\s*:?\\s*([^|\\n]+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String resolveFinancialCurrency(String resolved,
                                            BdrIndicadoresDTO indicadores,
                                            String priceCurrency) {
        if (resolved != null && !resolved.isBlank()) {
            return resolved;
        }
        if (indicadores != null && indicadores.moedaPadrao() != null && !indicadores.moedaPadrao().isBlank()) {
            return indicadores.moedaPadrao();
        }
        if (priceCurrency != null && !priceCurrency.isBlank()) {
            return priceCurrency;
        }
        return "USD";
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
        current.setPEbitda(findSimpleDecimal(indicadores, "P/EBITDA", "P EBITDA"));
        current.setPAtivo(findSimpleDecimal(indicadores, "P/ATIVO", "P ATIVO"));
        current.setRoe(findPercentual(indicadores, "ROE", "RETORNO SOBRE PATRIMONIO", "RETORNO SOBRE PATRIMÔNIO"));
        current.setRoic(findPercentual(indicadores, "ROIC", "RETORNO SOBRE CAPITAL INVESTIDO"));
        current.setRoa(findPercentual(indicadores, "ROA", "RETORNO SOBRE ATIVOS"));
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
        BigDecimal margemOperacional = findPercentual(indicadores,
                "MARGEM OPERACIONAL",
                "MARGEM EBIT",
                "OPERATING MARGIN",
                "EBIT MARGIN");
        BigDecimal margemLiquida = findPercentual(indicadores, "MARGEM LIQUIDA", "MARGEM LÍQUIDA", "NET MARGIN");

        if (margemBruta == null && margemOperacional == null && margemLiquida == null) {
            return null;
        }

        CurrentMargins margens = new CurrentMargins();
        margens.setMargemBruta(margemBruta);
        margens.setMargemOperacional(margemOperacional);
        margens.setMargemLiquida(margemLiquida);
        return margens;
    }

    private BdrMarketCap mapMarketCap(BdrIndicadoresDTO indicadores) {
        if (indicadores == null) {
            return null;
        }
        BigDecimal value = findMonetario(indicadores, "VALOR DE MERCADO", "MARKET CAP");
        Optional<JsonNode> node = indicadores.raw() == null
                ? Optional.empty()
                : findIndicatorNode(indicadores.raw(), "VALOR DE MERCADO", "MARKET CAP");
        String raw = node.map(this::extractIndicatorRawValue).orElse(null);
        if (value == null && raw != null) {
            value = IndicadorParser.parseValorMonetario(raw).orElse(null);
        }
        if (value == null && raw == null) {
            return null;
        }
        BdrMarketCap marketCap = new BdrMarketCap();
        marketCap.setValue(value);
        String currency = node.map(n -> resolveCurrencyFromNode(n).orElse(null))
                .orElseGet(() -> IndicadorParser.extrairMoeda(raw).orElse(indicadores.moedaPadrao()));
        if (currency != null) {
            marketCap.setCurrency(currency);
        }
        String quality = node.map(n -> extractQuality(n).orElse(null)).orElse(null);
        marketCap.setQuality(quality != null ? quality : "api_historico_indicadores");
        marketCap.setRaw(raw);
        return marketCap;
    }

    private ParidadeBdr mapParidade(IndicadorParser.ParidadeBdrInfo info) {
        if (info == null) {
            return null;
        }
        ParidadeBdr paridade = new ParidadeBdr();
        paridade.setRatio(calculateRatio(info));
        paridade.setMethod(ParidadeMethod.SOURCE_HTML);
        if (paridade.getRatio() != null) {
            paridade.setConfidence(BigDecimal.ONE);
        }
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
        if (dividendos == null) {
            return List.of();
        }
        Map<Integer, BigDecimal> acumuladoPorAno = new TreeMap<>();
        Map<Integer, String> currencyPorAno = new HashMap<>();
        if (dividendos.dividendos() != null) {
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
        }

        Map<Integer, BigDecimal> dividendYieldPorAno = extractDividendYieldByYear(dividendos.raw());

        Set<Integer> anos = new TreeSet<>();
        anos.addAll(acumuladoPorAno.keySet());
        anos.addAll(dividendYieldPorAno.keySet());

        return anos.stream()
                .map(ano -> {
                    DividendYear year = new DividendYear();
                    year.setYear(ano);
                    BigDecimal total = acumuladoPorAno.get(ano);
                    if (total != null) {
                        year.setValor(total.setScale(4, RoundingMode.HALF_UP));
                    }
                    year.setDividendYield(dividendYieldPorAno.get(ano));
                    String currency = currencyPorAno.getOrDefault(ano, "USD");
                    year.setCurrency(currency == null ? null : currency.trim().toUpperCase(Locale.ROOT));
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

    private Map<Integer, BigDecimal> extractDividendYieldByYear(JsonNode raw) {
        if (raw == null || raw.isNull()) {
            return Map.of();
        }
        SeriesStructure structure = extractSeriesStructure(raw);
        if (structure.datasets().isEmpty()) {
            return Map.of();
        }
        Map<Integer, BigDecimal> yields = new HashMap<>();
        for (JsonNode dataset : structure.datasets()) {
            String label = extractIndicatorName(dataset);
            if (label == null) {
                continue;
            }
            String normalized = IndicadorParser.normalizar(label);
            if (!matchesIndicator(normalized, "DIVIDEND YIELD", "YIELD", "DY")) {
                continue;
            }
            Map<Integer, ValueWithMeta> values = parseDataset(dataset, structure.years());
            for (Map.Entry<Integer, ValueWithMeta> entry : values.entrySet()) {
                ValueWithMeta meta = entry.getValue();
                if (meta == null || meta.value() == null) {
                    continue;
                }
                BigDecimal yield = parseDividendYieldValue(meta);
                if (yield != null) {
                    yields.put(entry.getKey(), yield);
                }
            }
        }
        return yields;
    }

    private List<DreYear> mapDreYears(BdrDemonstrativoDTO dre,
                                      AtomicReference<String> currencyRef) {
        if (dre == null || dre.raw() == null) {
            return List.of();
        }
        SeriesStructure structure = extractSeriesStructure(dre.raw());
        if (structure.datasets().isEmpty()) {
            return List.of();
        }
        Map<Integer, DreYear> years = new TreeMap<>();
        for (JsonNode dataset : structure.datasets()) {
            String label = extractIndicatorName(dataset);
            if (label == null) {
                continue;
            }
            String normalized = IndicadorParser.normalizar(label);
            Map<Integer, ValueWithMeta> values = parseDataset(dataset, structure.years());
            for (Map.Entry<Integer, ValueWithMeta> entry : values.entrySet()) {
                DreYear year = years.computeIfAbsent(entry.getKey(), key -> {
                    DreYear y = new DreYear();
                    y.setAno(key);
                    return y;
                });
                ValueWithMeta meta = entry.getValue();
                updateCurrencyReference(currencyRef, meta.currency());
                if (matchesIndicator(normalized, "RECEITA TOTAL", "RECEITA LIQUIDA", "TOTAL REVENUE", "NET REVENUE", "REVENUE")) {
                    year.setReceitaTotalUsd(toDreMetric(meta));
                } else if (matchesIndicator(normalized, "LUCRO BRUTO", "GROSS PROFIT")) {
                    year.setLucroBrutoUsd(toDreMetric(meta));
                } else if (matchesIndicator(normalized, "EBITDA")) {
                    year.setEbitdaUsd(toDreMetric(meta));
                } else if (matchesIndicator(normalized, "EBIT", "OPERATING INCOME", "OPERATING PROFIT")) {
                    year.setEbitUsd(toDreMetric(meta));
                } else if (matchesIndicator(normalized, "LUCRO LIQUIDO", "NET INCOME", "LUCRO LIQUIDO AJUSTADO")) {
                    year.setLucroLiquidoUsd(toDreMetric(meta));
                }
            }
        }
        return years.values().stream()
                .sorted(Comparator.comparing(DreYear::getAno, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }

    private List<BpYear> mapBpYears(BdrDemonstrativoDTO bp,
                                    AtomicReference<String> currencyRef) {
        if (bp == null || bp.raw() == null) {
            return List.of();
        }
        SeriesStructure structure = extractSeriesStructure(bp.raw());
        if (structure.datasets().isEmpty()) {
            return List.of();
        }
        Map<Integer, BpYear> years = new TreeMap<>();
        for (JsonNode dataset : structure.datasets()) {
            String label = extractIndicatorName(dataset);
            if (label == null) {
                continue;
            }
            String normalized = IndicadorParser.normalizar(label);
            Map<Integer, ValueWithMeta> values = parseDataset(dataset, structure.years());
            for (Map.Entry<Integer, ValueWithMeta> entry : values.entrySet()) {
                BpYear year = years.computeIfAbsent(entry.getKey(), key -> {
                    BpYear y = new BpYear();
                    y.setAno(key);
                    return y;
                });
                ValueWithMeta meta = entry.getValue();
                updateCurrencyReference(currencyRef, meta.currency());
                if (matchesIndicator(normalized, "ATIVOS TOTAIS", "TOTAL ASSETS")) {
                    year.setAtivosTotais(toAuditedValue(meta));
                } else if (matchesIndicator(normalized, "PASSIVOS TOTAIS", "TOTAL LIABILITIES")) {
                    year.setPassivosTotais(toAuditedValue(meta));
                } else if (matchesIndicator(normalized, "DIVIDA LONGO PRAZO", "DIVIDA DE LONGO PRAZO", "LONG TERM DEBT")) {
                    year.setDividaLongoPrazo(toAuditedValue(meta));
                } else if (matchesIndicator(normalized, "PATRIMONIO LIQUIDO", "EQUITY", "SHAREHOLDERS EQUITY")) {
                    year.setPl(toAuditedValue(meta));
                }
            }
        }
        return years.values().stream()
                .sorted(Comparator.comparing(BpYear::getAno, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }

    private List<FcYear> mapFcYears(BdrDemonstrativoDTO fc,
                                    AtomicReference<String> currencyRef) {
        if (fc == null || fc.raw() == null) {
            return List.of();
        }
        SeriesStructure structure = extractSeriesStructure(fc.raw());
        if (structure.datasets().isEmpty()) {
            return List.of();
        }
        Map<Integer, FcYear> years = new TreeMap<>();
        for (JsonNode dataset : structure.datasets()) {
            String label = extractIndicatorName(dataset);
            if (label == null) {
                continue;
            }
            String normalized = IndicadorParser.normalizar(label);
            Map<Integer, ValueWithMeta> values = parseDataset(dataset, structure.years());
            for (Map.Entry<Integer, ValueWithMeta> entry : values.entrySet()) {
                FcYear year = years.computeIfAbsent(entry.getKey(), key -> {
                    FcYear y = new FcYear();
                    y.setAno(key);
                    return y;
                });
                ValueWithMeta meta = entry.getValue();
                updateCurrencyReference(currencyRef, meta.currency());
                if (matchesIndicator(normalized, "FLUXO CAIXA OPERACIONAL", "OPERATING CASH FLOW", "CASH FROM OPERATIONS")) {
                    year.setFluxoCaixaOperacional(toQualityValue(meta));
                } else if (matchesIndicator(normalized, "FLUXO CAIXA INVESTIMENTO", "INVESTING CASH FLOW")) {
                    year.setFluxoCaixaInvestimento(toQualityValue(meta));
                } else if (matchesIndicator(normalized, "FLUXO CAIXA FINANCIAMENTO", "FINANCING CASH FLOW")) {
                    year.setFluxoCaixaFinanciamento(toQualityValue(meta));
                }
            }
        }
        return years.values().stream()
                .sorted(Comparator.comparing(FcYear::getAno, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }

    private BigDecimal parseDividendYieldValue(ValueWithMeta meta) {
        if (meta == null) {
            return null;
        }
        if (meta.raw() != null) {
            return IndicadorParser.parsePercentualParaDecimal(meta.raw())
                    .orElseGet(() -> normalizeYield(meta.value()));
        }
        return normalizeYield(meta.value());
    }

    private BigDecimal normalizeYield(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ONE) > 0) {
            return value.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        }
        return value;
    }

    private DreYear.Metric toDreMetric(ValueWithMeta meta) {
        if (meta == null || meta.value() == null) {
            return null;
        }
        DreYear.Metric metric = new DreYear.Metric();
        metric.setValue(meta.value());
        metric.setRaw(meta.raw());
        metric.setQuality(meta.quality());
        return metric;
    }

    private AuditedValue toAuditedValue(ValueWithMeta meta) {
        if (meta == null || meta.value() == null) {
            return null;
        }
        AuditedValue value = new AuditedValue();
        value.setValue(meta.value());
        value.setRaw(meta.raw());
        value.setQuality(meta.quality());
        return value;
    }

    private QualityValue toQualityValue(ValueWithMeta meta) {
        if (meta == null || meta.value() == null) {
            return null;
        }
        QualityValue value = new QualityValue();
        value.setValue(meta.value());
        value.setRaw(meta.raw());
        value.setQuality(meta.quality());
        return value;
    }

    private void updateCurrencyReference(AtomicReference<String> ref, String currency) {
        if (ref == null || currency == null || currency.isBlank()) {
            return;
        }
        ref.compareAndSet(null, currency.trim().toUpperCase(Locale.ROOT));
    }

    private boolean matchesIndicator(String normalizedCandidate, String... expected) {
        if (normalizedCandidate == null || normalizedCandidate.isBlank() || expected == null) {
            return false;
        }
        for (String option : expected) {
            if (option == null) {
                continue;
            }
            if (normalizedCandidate.equals(IndicadorParser.normalizar(option))) {
                return true;
            }
        }
        return false;
    }

    private SeriesStructure extractSeriesStructure(JsonNode raw) {
        if (raw == null || raw.isNull()) {
            return new SeriesStructure(List.of(), List.of());
        }
        JsonNode dataNode = raw.has("data") ? raw.get("data") : raw;
        List<Integer> years = extractLabelYears(dataNode.get("labels"));
        if (years.isEmpty() && dataNode.has("anos")) {
            years = extractLabelYears(dataNode.get("anos"));
        }
        List<JsonNode> datasets = new ArrayList<>();
        JsonNode datasetsNode = dataNode.get("datasets");
        if (datasetsNode == null) {
            datasetsNode = dataNode.get("series");
        }
        if (datasetsNode == null) {
            datasetsNode = dataNode.get("dataset");
        }
        if (datasetsNode != null && datasetsNode.isArray()) {
            datasetsNode.forEach(datasets::add);
        } else if (datasetsNode != null && datasetsNode.isObject()) {
            datasets.add(datasetsNode);
        } else if (dataNode.isArray()) {
            dataNode.forEach(datasets::add);
        }
        return new SeriesStructure(List.copyOf(years), List.copyOf(datasets));
    }

    private List<Integer> extractLabelYears(JsonNode labelsNode) {
        if (labelsNode == null || !labelsNode.isArray()) {
            return new ArrayList<>();
        }
        List<Integer> years = new ArrayList<>();
        for (JsonNode label : labelsNode) {
            Integer year = extractYear(label);
            if (year != null) {
                years.add(year);
            }
        }
        return years;
    }

    private Map<Integer, ValueWithMeta> parseDataset(JsonNode dataset, List<Integer> labelYears) {
        Map<Integer, ValueWithMeta> values = new LinkedHashMap<>();
        if (dataset == null) {
            return values;
        }
        List<JsonNode> nodes = extractValueNodes(dataset);
        for (int i = 0; i < nodes.size(); i++) {
            JsonNode node = nodes.get(i);
            Integer year = null;
            if (node != null && node.isObject()) {
                year = extractYear(node.get("year"));
                if (year == null) {
                    year = extractYear(node.get("ano"));
                }
            }
            if ((year == null || year == 0) && labelYears != null && i < labelYears.size()) {
                year = labelYears.get(i);
            }
            if ((year == null || year == 0) && node != null) {
                year = extractYear(node);
            }
            ValueWithMeta meta = toValueWithMeta(node, dataset);
            if (year != null && meta != null && meta.value() != null) {
                values.put(year, meta);
            }
        }
        return values;
    }

    private List<JsonNode> extractValueNodes(JsonNode dataset) {
        JsonNode dataNode = dataset == null ? null : dataset.get("data");
        if (dataNode == null && dataset != null) {
            dataNode = dataset.get("values");
        }
        if (dataNode == null && dataset != null) {
            dataNode = dataset.get("serie");
        }
        if (dataNode == null && dataset != null) {
            dataNode = dataset.get("itens");
        }
        if (dataNode == null) {
            return List.of();
        }
        if (dataNode.isArray()) {
            List<JsonNode> result = new ArrayList<>();
            dataNode.forEach(result::add);
            return result;
        }
        return List.of(dataNode);
    }

    private ValueWithMeta toValueWithMeta(JsonNode valueNode, JsonNode dataset) {
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        BigDecimal value = null;
        String raw = null;
        Quality quality = Quality.UNKNOWN;
        String currency = null;

        if (valueNode.isNumber()) {
            value = valueNode.decimalValue();
        } else if (valueNode.isTextual()) {
            raw = valueNode.asText();
            value = IndicadorParser.parseValorMonetario(raw)
                    .orElseGet(() -> IndicadorParser.safeParseDouble(raw).map(BigDecimal::valueOf).orElse(null));
            if (value == null) {
                value = IndicadorParser.parsePercentualParaDecimal(raw).orElse(null);
            }
        } else if (valueNode.isObject()) {
            JsonNode rawNode = valueNode.get("raw");
            if (rawNode == null) {
                rawNode = valueNode.get("formatted");
            }
            if (rawNode != null && rawNode.isTextual()) {
                raw = rawNode.asText();
            }
            JsonNode valueField = valueNode.get("value");
            if (valueField == null) {
                valueField = valueNode.get("valor");
            }
            if (valueField == null) {
                valueField = valueNode.get("amount");
            }
            if (valueField != null) {
                if (valueField.isNumber()) {
                    value = valueField.decimalValue();
                } else if (valueField.isTextual()) {
                    String text = valueField.asText();
                    raw = raw == null ? text : raw;
                    value = IndicadorParser.parseValorMonetario(text)
                            .orElseGet(() -> IndicadorParser.safeParseDouble(text).map(BigDecimal::valueOf).orElse(null));
                    if (value == null) {
                        value = IndicadorParser.parsePercentualParaDecimal(text).orElse(null);
                    }
                }
            }
            if (value == null && raw != null) {
                value = IndicadorParser.parseValorMonetario(raw)
                        .orElseGet(() -> IndicadorParser.safeParseDouble(raw).map(BigDecimal::valueOf).orElse(null));
                if (value == null) {
                    value = IndicadorParser.parsePercentualParaDecimal(raw).orElse(null);
                }
            }
            if (currency == null) {
                currency = resolveCurrencyFromNode(valueNode).orElse(null);
            }
            quality = extractQuality(valueNode)
                    .map(Quality::fromValue)
                    .orElse(Quality.UNKNOWN);
        }

        if (currency == null) {
            currency = resolveCurrencyFromNode(dataset).orElse(null);
        }
        if (quality == Quality.UNKNOWN) {
            quality = extractQuality(dataset)
                    .map(Quality::fromValue)
                    .orElse(Quality.UNKNOWN);
        }
        if (raw == null && !valueNode.isNumber()) {
            raw = valueNode.toString();
        }
        if (value == null) {
            return null;
        }
        return new ValueWithMeta(value, raw, quality, currency == null ? null : currency.trim().toUpperCase(Locale.ROOT));
    }

    private Optional<String> resolveCurrencyFromNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return Optional.empty();
        }
        for (String field : List.of("currency", "moeda", "moedaPadrao")) {
            JsonNode value = node.get(field);
            if (value != null && value.isTextual()) {
                String text = value.asText();
                if (!text.isBlank()) {
                    return Optional.of(text.trim().toUpperCase(Locale.ROOT));
                }
            }
        }
        String raw = extractIndicatorRawValue(node);
        if (raw != null) {
            return IndicadorParser.extrairMoeda(raw);
        }
        return Optional.empty();
    }

    private Optional<String> extractQuality(JsonNode node) {
        if (node == null || node.isNull()) {
            return Optional.empty();
        }
        for (String field : List.of("quality", "qualidade")) {
            JsonNode value = node.get(field);
            if (value != null && value.isTextual()) {
                return Optional.of(value.asText());
            }
        }
        return Optional.empty();
    }

    private JsonNode extractHistoryArray(JsonNode raw) {
        if (raw == null || raw.isNull()) {
            return null;
        }
        if (raw.has("history")) {
            return raw.get("history");
        }
        if (raw.has("data") && raw.get("data").has("history")) {
            return raw.get("data").get("history");
        }
        if (raw.isArray()) {
            for (JsonNode node : raw) {
                JsonNode nested = extractHistoryArray(node);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private record SeriesStructure(List<Integer> years, List<JsonNode> datasets) {
    }

    private record ValueWithMeta(BigDecimal value, String raw, Quality quality, String currency) {
    }


    private List<HistoricalIndicator> mapHistoricalIndicators(BdrIndicadoresDTO indicadores) {
        if (indicadores == null || indicadores.raw() == null) {
            return List.of();
        }
        JsonNode historyNode = extractHistoryArray(indicadores.raw());
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
        history.sort(Comparator.comparing(HistoricalIndicator::getYear, Comparator.nullsLast(Integer::compareTo)));
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
