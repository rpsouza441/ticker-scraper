package br.dev.rodrigopinheiro.tickerscraper.infrastructure.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import java.util.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.mapper.BdrScraperMapper;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.FiiPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.FundoImobiliarioEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.*;
import static org.junit.jupiter.api.Assertions.*;

class DividendConversionRegressionTest {
    @Test void mixedAnnualPeriodsExcludeRollingAggregateAndPreserveAmounts() {
        var mapper=Mappers.getMapper(BdrScraperMapper.class);
        var dividends=mapper.mapDividendos(Map.of("content",List.of(
                Map.of("created_at",2024,"price",0.18),
                Map.of("created_at","2025","price",2),
                Map.of("created_at","Últ. 12M","price",0.19))));
        assertEquals(2,dividends.size());assertEquals(YearMonth.of(2025,1),dividends.get(1).getMes());
        assertEquals(0,new BigDecimal("2").compareTo(dividends.get(1).getValor()));
        assertEquals("BRL",dividends.getFirst().getMoeda());
    }
    @Test void unexpectedPeriodIsAnExplicitParsingError() {
        assertThrows(br.dev.rodrigopinheiro.tickerscraper.domain.exception.DataParsingException.class,
                ()->Mappers.getMapper(BdrScraperMapper.class).mapDividendos(Map.of("content",List.of(Map.of("created_at","unknown","price",0.19)))));
    }
    @Test void fiiReplacementDoesNotAppendPreviousMonthsAndKeepsChangedValues() {
        var mapper=Mappers.getMapper(FiiPersistenceMapper.class);
        FundoImobiliario source=new FundoImobiliario();
        source.setFiiDividendos(new ArrayList<>(List.of(new FiiDividendo(YearMonth.of(2026,8),new BigDecimal("1.17")))));
        FundoImobiliarioEntity entity=mapper.toEntity(source);
        mapper.replaceDividendos(source,entity);mapper.replaceDividendos(source,entity);
        assertEquals(1,entity.getFiiDividendos().size());
        assertEquals(new BigDecimal("1.17"),entity.getFiiDividendos().getFirst().getValor());
        assertSame(entity,entity.getFiiDividendos().getFirst().getFundoImobiliario());
    }
}
