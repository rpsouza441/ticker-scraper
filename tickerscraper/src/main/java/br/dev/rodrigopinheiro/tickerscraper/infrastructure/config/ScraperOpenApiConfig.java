package br.dev.rodrigopinheiro.tickerscraper.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

@Configuration
public class ScraperOpenApiConfig {
    @Bean
    OpenApiCustomizer scraperErrors() {
        return api -> api.getPaths().values().forEach(path -> path.readOperations().forEach(operation -> {
            for (String code : new String[]{"400", "404", "422", "429", "500", "503", "504"}) {
                String description = switch (code) {
                    case "400" -> "Ticker inválido";
                    case "404" -> "Ticker não encontrado na origem";
                    case "422" -> "Dados ou estrutura da origem incompatíveis";
                    case "429" -> "Limite de requisições na origem";
                    case "503" -> "Scraper ocupado ou indisponível";
                    case "504" -> "Tempo limite de processamento excedido";
                    default -> "Erro interno";
                };
                operation.getResponses().addApiResponse(code, new ApiResponse().description(description)
                        .content(new Content().addMediaType("application/json", new MediaType().schema(
                                new Schema<>().type("object")
                                        .addProperty("code", new Schema<>().type("string"))
                                        .addProperty("message", new Schema<>().type("string"))
                                        .addProperty("correlationId", new Schema<>().type("string"))
                                        .addProperty("timestamp", new Schema<>().type("string"))
                                        .addProperty("retryable", new Schema<>().type("boolean"))))));
            }
        }));
    }
}
