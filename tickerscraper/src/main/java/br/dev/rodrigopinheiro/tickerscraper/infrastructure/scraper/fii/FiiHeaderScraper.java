package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.domain.exception.ElementNotFoundException;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoHeaderDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.validator.ScraperValidator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Scraper responsável por extrair informações do cabeçalho de uma página de FII.
 * Utiliza o ScraperValidator para busca robusta de elementos e extração de texto.
 */
@Component
public class FiiHeaderScraper {
    private static final Logger logger = LoggerFactory.getLogger(FiiHeaderScraper.class);
    
    // Constantes para seletores CSS com fallbacks
    private static final String[] CONTAINER_SELECTORS = {"div.name-ticker", "div.container-header", "header div.fii-info"};
    private static final String[] TICKER_SELECTORS = {"h1", "span.ticker", ".ticker-symbol"};
    private static final String[] NOME_FII_SELECTORS = {"h2.name-company", "span.fii-name", ".fii-title"};
    
    /**
     * Raspa a seção do cabeçalho de uma página de FII.
     * Utiliza seletores com fallbacks para maior robustez.
     *
     * @param doc O documento HTML completo da página.
     * @return Um DTO com o ticker e o nome do FII.
     * @throws ElementNotFoundException se elementos essenciais não forem encontrados
     */
    public FiiInfoHeaderDTO scrape(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Documento HTML não pode ser nulo");
        }
        
        logger.debug("Iniciando extração de informações do cabeçalho de FII");
        
        // Busca o container com fallbacks
        Element container = ScraperValidator.findElementWithFallbacks(doc, CONTAINER_SELECTORS)
                .orElseThrow(() -> {
                    logger.error("Container do cabeçalho não encontrado");
                    return ElementNotFoundException.forSelectors(CONTAINER_SELECTORS);
                });
        
        // Extrai o ticker com fallbacks
        String ticker = ScraperValidator.findElementWithFallbacks(container, TICKER_SELECTORS)
                .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                .orElse("N/A");
        
        // Extrai o nome do FII com fallbacks
        String nomeFii = ScraperValidator.findElementWithFallbacks(container, NOME_FII_SELECTORS)
                .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                .orElse("N/A");
        
        logger.debug("Informações extraídas: ticker={}, nomeFii={}", ticker, nomeFii);
        return new FiiInfoHeaderDTO(ticker, nomeFii);
    }
}
