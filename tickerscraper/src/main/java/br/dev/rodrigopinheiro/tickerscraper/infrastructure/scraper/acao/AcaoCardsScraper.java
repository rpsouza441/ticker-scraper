package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.domain.exception.ElementNotFoundException;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoInfoCardsDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.validator.ScraperValidator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Scraper responsável por extrair informações dos cards de uma ação.
 * Utiliza o ScraperValidator para busca robusta de elementos e extração de texto.
 */
@Component
public class AcaoCardsScraper {
    private static final Logger logger = LoggerFactory.getLogger(AcaoCardsScraper.class);
    
    // Constantes para seletores CSS com fallbacks
    private static final String[] CONTAINER_SELECTORS = {"section#cards-ticker", ".cards-section", ".ticker-cards"};
    private static final String[] COTACAO_SELECTORS = {"div._card.cotacao div._card-body span.value", ".cotacao .value", ".price-value"};
    private static final String[] VARIACAO_SELECTORS = {"div._card.pl div._card-body span", ".pl .value", ".variation-value"};
    /**
     * Extrai informações dos cards de uma página de ação.
     * Utiliza seletores com fallbacks para maior robustez.
     *
     * @param doc O documento HTML da página
     * @return DTO com as informações dos cards
     * @throws ElementNotFoundException se elementos essenciais não forem encontrados
     */
    public AcaoInfoCardsDTO scrapeCardsInfo(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Documento HTML não pode ser nulo");
        }
        
        logger.debug("Iniciando extração de informações dos cards da ação");
        
        // Busca o container com fallbacks
        Element container = ScraperValidator.findElementWithFallbacks(doc, CONTAINER_SELECTORS)
                .orElseThrow(() -> {
                    logger.error("Container dos cards não encontrado");
                    return ElementNotFoundException.forSelectors(CONTAINER_SELECTORS);
                });
        
        // Extrai a cotação com fallbacks
        String cotacao = ScraperValidator.findElementWithFallbacks(container, COTACAO_SELECTORS)
                .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                .orElse("N/A");
        
        // Extrai a variação 12M com fallbacks
        String variacao12M = ScraperValidator.findElementWithFallbacks(container, VARIACAO_SELECTORS)
                .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                .orElse("N/A");
        
        logger.debug("Informações extraídas: cotacao={}, variacao12M={}", cotacao, variacao12M);
        return new AcaoInfoCardsDTO(cotacao, variacao12M);
    }
}
