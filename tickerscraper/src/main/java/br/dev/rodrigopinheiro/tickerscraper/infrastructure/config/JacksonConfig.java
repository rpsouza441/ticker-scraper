package br.dev.rodrigopinheiro.tickerscraper.infrastructure.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Configuração do Jackson para padronizar serialização de tipos numéricos.
 * Resolve inconsistências na formatação de BigDecimal entre dados de scraping e banco.
 */
@Configuration
public class JacksonConfig {

    /**
     * Serializer customizado para BigDecimal que remove zeros desnecessários.
     */
    public static class BigDecimalPlainSerializer extends JsonSerializer<BigDecimal> {
        @Override
        public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                // Usa toPlainString() que remove notação científica e zeros desnecessários
                gen.writeNumber(value.stripTrailingZeros().toPlainString());
            }
        }
    }

    /**
     * Configura ObjectMapper personalizado para serializar BigDecimal sem casas decimais desnecessárias.
     * Evita diferenças entre "1000000" (scraping) e "1000000.00" (banco de dados).
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.build();
        
        SimpleModule module = new SimpleModule("BigDecimalModule");
        module.addSerializer(BigDecimal.class, new BigDecimalPlainSerializer());
        
        mapper.registerModule(module);
        
        return mapper;
    }
}