package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.bdr.converter;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.ParidadeMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ParidadeMethodAttributeConverter implements AttributeConverter<ParidadeMethod, String> {

    @Override
    public String convertToDatabaseColumn(ParidadeMethod attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public ParidadeMethod convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ParidadeMethod.valueOf(dbData);
    }
}
