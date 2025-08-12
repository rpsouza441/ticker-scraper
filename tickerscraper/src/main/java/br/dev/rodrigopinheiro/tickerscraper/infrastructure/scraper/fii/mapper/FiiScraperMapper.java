package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.mapper;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.FiiDividendo;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiIndicadorHistoricoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiIndicadorHistoricoItemDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDividendoDTO;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", uses = { IndicadorParser.class })
public interface FiiScraperMapper {

    // -------- DTO → Domain (FundoImobiliario) --------
    @Mappings({
            // Header
            @Mapping(source = "infoHeader.ticker",      target = "ticker",      qualifiedByName = "limpezaComUpperCase"),
            @Mapping(source = "infoHeader.nomeEmpresa", target = "nomeEmpresa"),

            // Sobre (strings diretas)
            @Mapping(source = "infoSobre.razaoSocial",     target = "razaoSocial"),
            @Mapping(source = "infoSobre.cnpj",            target = "cnpj"),
            @Mapping(source = "infoSobre.publicoAlvo",     target = "publicoAlvo"),
            @Mapping(source = "infoSobre.mandato",         target = "mandato"),
            @Mapping(source = "infoSobre.segmento",        target = "segmento"),
            @Mapping(source = "infoSobre.tipoDeFundo",     target = "tipoDeFundo"),
            @Mapping(source = "infoSobre.prazoDeDuracao",  target = "prazoDeDuracao"),
            @Mapping(source = "infoSobre.tipoDeGestao",    target = "tipoDeGestao"),

            // Sobre (numéricos via parser)
            @Mapping(source = "infoSobre.taxaDeAdministracao", target = "taxaDeAdministracao", qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoSobre.ultimoRendimento",     target = "ultimoRendimento",    qualifiedByName = "paraBigDecimal"),

            // Cards
            @Mapping(source = "infoCards.cotacao",     target = "cotacao",     qualifiedByName = "paraBigDecimal"),
            @Mapping(source = "infoCards.variacao12M", target = "variacao12M", qualifiedByName = "paraBigDecimal"),

            // Histórico “Atual” (fonte de verdade numérica)
            @Mapping(target = "valorDeMercado",          expression = "java(pickAtual(dados.infoHistorico(), \"VALOR DE MERCADO\", \"enterprise_value\"))"),
            @Mapping(target = "pvp",                     expression = "java(pickAtual(dados.infoHistorico(), \"P/VP\", \"p_vp\"))"),
            @Mapping(target = "dividendYield",           expression = "java(pickAtual(dados.infoHistorico(), \"DIVIDEND YIELD (DY)\", \"dividend_yield_last_12_months\"))"),
            @Mapping(target = "liquidezDiaria",          expression = "java(pickAtual(dados.infoHistorico(), \"LIQUIDEZ DIÁRIA\", \"daily_liquidity\"))"),
            @Mapping(target = "valorPatrimonial",        expression = "java(pickAtual(dados.infoHistorico(), \"VALOR PATRIMONIAL\", \"equity_value\"))"),
            @Mapping(target = "valorPatrimonialPorCota", expression = "java(pickAtual(dados.infoHistorico(), \"VAL. PATRIMONIAL P/ COTA\", \"equity_value_account\"))"),
            @Mapping(target = "vacancia",                expression = "java(pickAtual(dados.infoHistorico(), \"VACÂNCIA\", \"occupancy_rate\"))"),
            @Mapping(target = "numeroDeCotistas",        expression = "java(toLong(pickAtual(dados.infoHistorico(), \"NÚMERO DE COTISTAS\", \"shareholders_count\")))"),
            @Mapping(target = "cotasEmitidas",           expression = "java(toLong(pickAtual(dados.infoHistorico(), \"COTAS EMITIDAS\", \"quote_count\")))"),

            // Dividendos (lista)
            @Mapping(source = "dividendos", target = "fiiDividendos")
    })
    FundoImobiliario toDomain(FiiDadosFinanceirosDTO dados);

    // -------- elemento da lista: FiiDividendoDTO → FiiDividendo (domain) --------
    @Mappings({
            // ajuste aqui se o seu record usa nomes diferentes:
            // ex.: se for createdAt() String "MM/yyyy", troque para um helper que parseia YearMonth
            @Mapping(source = "created_at", target = "mes"),
            @Mapping(source = "price",        target = "valor")
    })
    FiiDividendo toDomain(FiiDividendoDTO dto);

    // -------- pós-processamento: fallback do último rendimento --------
    @AfterMapping
    default void fallbackUltimoRendimento(FiiDadosFinanceirosDTO src, @MappingTarget FundoImobiliario target) {
        if (target.getUltimoRendimento() == null && target.getFiiDividendos() != null && !target.getFiiDividendos().isEmpty()) {
            BigDecimal ultimo = target.getFiiDividendos().stream()
                    .max(Comparator.comparing(FiiDividendo::getMes))
                    .map(FiiDividendo::getValor)
                    .orElse(null);
            target.setUltimoRendimento(ultimo);
        }
    }

    // -------- helpers internos (não dependem de classe externa no impl gerado) --------
    default BigDecimal pickAtual(FiiIndicadorHistoricoDTO historico, String grupo, String chave) {
        if (historico == null) return null;
        Map<String, List<FiiIndicadorHistoricoItemDTO>> map = historico.indicadores(); // ajuste se o accessor for outro
        if (map == null) return null;
        List<FiiIndicadorHistoricoItemDTO> itens = map.get(grupo);
        if (itens == null) return null;

        return itens.stream()
                .filter(i -> i != null && "Atual".equalsIgnoreCase(i.year()) && chave.equals(i.key()) && i.value() != null)
                .findFirst()
                .map(FiiIndicadorHistoricoItemDTO::value) // já é BigDecimal nos seus DTOs
                .orElse(null);
    }

    default Long toLong(BigDecimal v) {
        return v == null ? null : v.longValue();
    }
}
