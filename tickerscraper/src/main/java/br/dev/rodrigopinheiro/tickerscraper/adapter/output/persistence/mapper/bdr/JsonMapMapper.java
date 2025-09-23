package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.bdr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface JsonMapMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Named("mapToJson")
    default String mapToJson(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize rawJson map", e);
        }
    }

    @Named("jsonToMap")
    default Map<String, Object> jsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to deserialize rawJson json", e);
        }
    }
}
