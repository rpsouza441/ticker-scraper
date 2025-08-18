package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.domain.exception.ElementNotFoundException;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoInfoHeaderDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.validator.ScraperValidator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Scraper responsável por extrair informações do cabeçalho de uma página de ação.
 * Utiliza o ScraperValidator para busca robusta de elementos e extração de texto.
 */
@Component
public class AcaoHeaderScraper {
    private static final Logger logger = LoggerFactory.getLogger(AcaoHeaderScraper.class);
    
    // Constantes para seletores CSS com fallbacks
    private static final String[] CONTAINER_SELECTORS = {"div.name-ticker", "div.container-header", "header div.company-info"};
    private static final String[] TICKER_SELECTORS = {"h1", "span.ticker", ".ticker-symbol"};
    private static final String[] NOME_EMPRESA_SELECTORS = {"h2.name-company", "span.company-name", ".company-title"};
    
    /**
     * Extrai informações do cabeçalho da página de uma ação.
     * Utiliza seletores com fallbacks para maior robustez.
     *
     * @param doc O documento HTML da página
     * @return DTO com as informações do cabeçalho
     * @throws ElementNotFoundException se elementos essenciais não forem encontrados
     */
    public AcaoInfoHeaderDTO scrapeInfoHeader(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Documento HTML não pode ser nulo");
        }
        
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
        
        // Extrai o nome da empresa com fallbacks
        String nomeEmpresa = ScraperValidator.findElementWithFallbacks(container, NOME_EMPRESA_SELECTORS)
                .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                .orElse("N/A");
        
        logger.debug("Informações extraídas: ticker={}, nomeEmpresa={}", ticker, nomeEmpresa);
        return new AcaoInfoHeaderDTO(ticker, nomeEmpresa);
    }
}
