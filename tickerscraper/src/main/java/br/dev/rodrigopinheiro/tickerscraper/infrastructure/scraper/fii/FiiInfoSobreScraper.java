package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.domain.exception.ElementNotFoundException;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoSobreDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.validator.ScraperValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Scraper responsável por extrair informações da seção "Sobre" de uma página de FII.
 * Utiliza o ScraperValidator para busca robusta de elementos e extração de texto.
 */
@Component
public class FiiInfoSobreScraper {
    private static final Logger logger = LoggerFactory.getLogger(FiiInfoSobreScraper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Constantes para seletores CSS com fallbacks
    private static final String[] CONTAINER_SELECTORS = {"div#about-company div.content", "div.about-section div.content", ".fii-about-info"};
    private static final String[] CELL_SELECTORS = {"div.cell", ".info-cell", ".data-item"};
    private static final String[] TITLE_SELECTORS = {"span.name", ".info-title", ".label"};
    private static final String[] VALUE_SELECTORS = {"div.value span", ".info-value", ".value"};
    



    /**
     * Extrai informações da seção "Sobre" de uma página de FII.
     * Utiliza seletores com fallbacks para maior robustez.
     *
     * @param doc O documento HTML da página
     * @return DTO com as informações da seção "Sobre"
     * @throws ElementNotFoundException se elementos essenciais não forem encontrados
     */
    public FiiInfoSobreDTO scrape(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Documento HTML não pode ser nulo");
        }
        
        logger.debug("Iniciando extração de informações da seção 'Sobre' do FII");
        
        // 1. Encontra o contêiner principal da seção "Informações Sobre" com fallbacks
        Element container = ScraperValidator.findElementWithFallbacks(doc, CONTAINER_SELECTORS)
                .orElseThrow(() -> {
                    logger.error("Container da seção 'Sobre' não encontrado");
                    return ElementNotFoundException.forSelectors(CONTAINER_SELECTORS);
                });
        
        // 2. Processa todas as células de informação
        Map<String, String> infoMap = container.select(CELL_SELECTORS[0]).stream()
                .map(cell -> {
                    // 3. Extrai o título com fallbacks
                    String tituloBruto = ScraperValidator.findElementWithFallbacks(cell, TITLE_SELECTORS)
                            .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                            .map(String::trim)
                            .orElse("");
                    
                    // 4. Extrai o valor com fallbacks
                    String valor = ScraperValidator.findElementWithFallbacks(cell, VALUE_SELECTORS)
                            .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                            .map(String::trim)
                            .orElse("");
                    
                    // 5. Normaliza o título usando o IndicadorParser
                    String titulo = IndicadorParser.normalizar(tituloBruto);
                    
                    logger.debug("Informação extraída: {}={}", titulo, valor);
                    return new AbstractMap.SimpleEntry<>(titulo, valor);
                })
                .filter(entry -> !entry.getKey().isEmpty() && !entry.getValue().isEmpty())
                .collect(toMap(
                        AbstractMap.SimpleEntry::getKey,
                        AbstractMap.SimpleEntry::getValue,
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new
                ));
        
        if (infoMap.isEmpty()) {
            logger.warn("Nenhuma informação encontrada na seção 'Sobre'");
        }

        return objectMapper.convertValue(infoMap, FiiInfoSobreDTO.class);
    }
}
