package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.domain.exception.ElementNotFoundException;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoInfoDetailedDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.validator.ScraperValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Scraper responsável por extrair informações detalhadas de uma ação.
 * Utiliza o ScraperValidator para busca robusta de elementos e extração de texto.
 */
@Component
public class AcaoDetailedInfoScraper {
    private static final Logger logger = LoggerFactory.getLogger(AcaoDetailedInfoScraper.class);
    
    // Constantes para seletores CSS com fallbacks
    private static final String[] CONTAINER_SELECTORS = {"div#info_about div.content", "div.info-about div.content", ".company-details"};
    private static final String[] CELL_SELECTORS = {"div.cell", ".info-cell", ".detail-item"};
    private static final String[] TITLE_SELECTORS = {"span.title", ".info-title", ".label"};
    private static final String[] VALUE_SELECTORS = {"div.detail-value", "span.value", ".info-value", ".value"};

    /**
     * Extrai informações detalhadas de uma página de ação.
     * Utiliza seletores com fallbacks para maior robustez.
     *
     * @param doc O documento HTML da página
     * @return DTO com as informações detalhadas
     * @throws ElementNotFoundException se elementos essenciais não forem encontrados
     */
    public AcaoInfoDetailedDTO scrapeAndParseDetailedInfo(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Documento HTML não pode ser nulo");
        }
        
        logger.debug("Iniciando extração de informações detalhadas da ação");
        
        // Busca o container com fallbacks
        Element container = ScraperValidator.findElementWithFallbacks(doc, CONTAINER_SELECTORS)
                .orElseThrow(() -> {
                    logger.error("Container de informações detalhadas não encontrado");
                    return ElementNotFoundException.forSelectors(CONTAINER_SELECTORS);
                });
        
        // Processa todas as células de informação
        Map<String, String> detailsMap = container.select(CELL_SELECTORS[0]).stream()
                .map(cell -> {
                    // Extrai o título com fallbacks
                    String title = ScraperValidator.findElementWithFallbacks(cell, TITLE_SELECTORS)
                            .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                            .orElse("");
                    
                    // Extrai o valor com fallbacks
                    String value = ScraperValidator.findElementWithFallbacks(cell, VALUE_SELECTORS)
                            .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                            .orElse("");
                    
                    logger.debug("Informação extraída: {}={}", title, value);
                    return new SimpleEntry<>(title, value);
                })
                .filter(entry -> !entry.getKey().isEmpty() && !entry.getValue().isEmpty())
                .collect(toMap(
                        SimpleEntry::getKey,
                        SimpleEntry::getValue,
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new
                ));
        
        if (detailsMap.isEmpty()) {
            logger.warn("Nenhuma informação detalhada encontrada");
        }


        final ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(detailsMap, AcaoInfoDetailedDTO.class);
    }
}