package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.shared;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = false)
public class YearMonthAttributeConverter implements AttributeConverter<YearMonth, String> {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM");
    @Override public String convertToDatabaseColumn(YearMonth attribute) {
        return attribute == null ? null : attribute.format(F);
    }
    @Override public YearMonth convertToEntityAttribute(String dbData) {
        return (dbData == null || dbData.isBlank()) ? null : YearMonth.parse(dbData, F);
    }
}