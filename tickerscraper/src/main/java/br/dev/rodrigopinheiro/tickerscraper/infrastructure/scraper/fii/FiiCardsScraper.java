package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoCardsDTO;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.validator.ScraperValidator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Scraper responsável por extrair informações dos cards de uma página de FII.
 * Utiliza o ScraperValidator para busca robusta de elementos e extração de texto.
 */
@Component
public class FiiCardsScraper {
    private static final Logger logger = LoggerFactory.getLogger(FiiCardsScraper.class);
    
    // Constantes para seletores CSS com fallbacks
    private static final String[] COTACAO_SELECTORS = {"div._card.cotacao span.value", ".cotacao .value", ".price-value"};
    private static final String[] VARIACAO_CONTAINER_SELECTORS = {"div._card:has(span[title='Variação (12M)'])", "div._card.variacao", ".variation-card"};
    private static final String[] VARIACAO_VALUE_SELECTORS = {"div._card-body span", ".card-body .value", ".variation-value"};

    /**
     * Extrai informações dos cards de uma página de FII.
     * Utiliza seletores com fallbacks para maior robustez.
     *
     * @param doc O documento HTML da página
     * @return DTO com as informações dos cards
     */
    public FiiInfoCardsDTO scrape(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Documento HTML não pode ser nulo");
        }
        
        logger.debug("Iniciando extração de informações dos cards do FII");
        
        // --- COTAÇÃO ---
        // A cotação já tem uma classe única ('cotacao'), então o seletor é direto.
        String cotacao = ScraperValidator.findElementWithFallbacks(doc, COTACAO_SELECTORS)
                .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                .orElse(""); // Deixaremos em branco, pois a cotação virá da API
        
        // --- VARIAÇÃO (12M) - A SOLUÇÃO ROBUSTA ---
        
        // 1. Encontra o contêiner do card usando a "âncora" do atributo 'title' com fallbacks
        Element cardVariacaoContainer = ScraperValidator.findElementWithFallbacks(doc, VARIACAO_CONTAINER_SELECTORS)
                .orElse(null);
        
        // 2. A partir do contêiner encontrado, busca o valor dentro do _card-body com fallbacks
        String variacao12M = "";
        if (cardVariacaoContainer != null) {
            variacao12M = ScraperValidator.findElementWithFallbacks(cardVariacaoContainer, VARIACAO_VALUE_SELECTORS)
                    .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                    .orElse("");
        }
        
        logger.debug("Informações extraídas: cotacao={}, variacao12M={}", cotacao, variacao12M);
        
        // Como a cotação virá da API de XHR, retornamos ela por enquanto.
        return new FiiInfoCardsDTO(cotacao, variacao12M);
    }
}