package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper;

import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import java.math.BigDecimal;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.AcaoIndicatorsScraper;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.BdrCardsScraper;
import static org.junit.jupiter.api.Assertions.*;

class ContentRegressionTest {
    @Test void indicatorNamesMatchAcrossCaseAccentsAndPunctuation() {
        var mapper=org.mapstruct.factory.Mappers.getMapper(br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.mapper.BdrScraperMapper.class);
        var indicators=java.util.Map.<String,Object>of("Dividend Yield (DY)",java.util.List.of(java.util.Map.of("year","Atual","value","1.60")));
        assertEquals(new BigDecimal("1.60"),mapper.getIndicatorValueAsPercent(indicators,"DIVIDEND YIELD (DY)"));
        var acao=org.mapstruct.factory.Mappers.getMapper(br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.mapper.AcaoScraperMapper.class);
        assertEquals(acao.normalizeIndicator("DÍVIDA LÍQUIDA / EBITDA"),acao.normalizeIndicator("Divida Liquida/Ebitda"));
        assertEquals(acao.normalizeIndicator("P/ATIVO CIRC LIQ"),acao.normalizeIndicator("P/Ativo Circ. Liq."));
    }
    @Test void readsCurrentIndicatorCardsIncludingWordBreaks() {
        var doc=Jsoup.parse("""
            <div id="table-indicators"><article class="indicator-card">
            <div class="indicator-card-title"><span>P/<wbr>L</span></div>
            <div class="indicator-card-value"><span>22,91</span></div>
            </article></div>
            """);
        var dto=new AcaoIndicatorsScraper().scrape(doc,"SAPR11");
        assertEquals("22,91",dto.indicadores().get("P/L").valor());
    }
    @Test void bdrQuoteComesFromQuoteCardAndNotDividendFaq() {
        var doc=Jsoup.parse("""
            <script type="application/ld+json">{"@type":"FAQPage","mainEntity":[{"acceptedAnswer":{"text":"Dividendo R$ 0,14"}}]}</script>
            <section id="cards-ticker"><div class="_card cotacao"><div class="_card-body">
            <div class="stockCurrentQuotation"><span class="value">R$ 86,39</span></div></div></div>
            <div class="_card pl"><div class="_card-body"><div><span>33,16%</span></div></div></div></section>
            """);
        var cards=new BdrCardsScraper().extract(doc);
        assertEquals(new BigDecimal("86.39"),cards.cotacao());assertEquals(33.16,cards.variacao12M());
    }
    @Test void thousandsDoNotSilentlyBecomeZeroOrChangeDecimalIndicators() {
        assertEquals(new BigDecimal("10653998000"),IndicadorParser.parseBigdecimal("R$ 10.653.998.000"));
        assertEquals(new BigDecimal("1511205000"),IndicadorParser.parseBigdecimal("1.511.205.000"));
        assertEquals(new BigDecimal("38.05"),IndicadorParser.parseBigdecimal("38.05"));
    }
}
