package br.dev.rodrigopinheiro.tickerscraper.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Quality {
    OK("ok"),
    MISSING("missing"),
    ZERO_REAL("zero_real"),
    UNKNOWN("unknown");

    private final String value;

    Quality(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Quality fromValue(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
                .filter(quality -> quality.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
