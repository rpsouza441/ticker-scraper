package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoHeaderDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.common.GenericHeaderScraper;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Scraper responsável por extrair informações do cabeçalho de uma página de FII.
 * 
 * REFATORADO: Agora utiliza o GenericHeaderScraper consolidado para eliminar duplicação.
 * Mantém compatibilidade total com o código existente.
 */
@Component
public class FiiHeaderScraper {
    private static final Logger logger = LoggerFactory.getLogger(FiiHeaderScraper.class);
    
    private final GenericHeaderScraper genericHeaderScraper;
    
    public FiiHeaderScraper(GenericHeaderScraper genericHeaderScraper) {
        this.genericHeaderScraper = genericHeaderScraper;
        logger.info("FiiHeaderScraper inicializado com GenericHeaderScraper consolidado");
    }
    
    /**
     * Raspa a seção do cabeçalho de uma página de FII.
     * 
     * REFATORADO: Agora delega para o GenericHeaderScraper consolidado.
     * Mantém a mesma assinatura e comportamento do método original.
     *
     * @param doc O documento HTML completo da página.
     * @return Um DTO com o ticker e o nome do FII.
     * @throws ElementNotFoundException se elementos essenciais não forem encontrados
     * @throws IllegalArgumentException se o documento for nulo
     */
    public FiiInfoHeaderDTO scrape(Document doc) {
        logger.debug("Delegando extração de cabeçalho de FII para GenericHeaderScraper");
        
        return genericHeaderScraper.scrapeFiiHeader(doc, FiiInfoHeaderDTO::new);
    }
}
