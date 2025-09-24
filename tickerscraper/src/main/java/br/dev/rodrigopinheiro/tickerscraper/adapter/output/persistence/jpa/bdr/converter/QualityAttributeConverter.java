package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.converter;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.Quality;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class QualityAttributeConverter implements AttributeConverter<Quality, String> {

    @Override
    public String convertToDatabaseColumn(Quality attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public Quality convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return Quality.UNKNOWN;
        }
        return Quality.fromValue(dbData);
    }
}
