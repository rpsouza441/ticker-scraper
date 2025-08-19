package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoInfoHeaderDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.common.GenericHeaderScraper;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Scraper responsável por extrair informações do cabeçalho de uma página de ação.
 * 
 * REFATORADO: Agora utiliza o GenericHeaderScraper consolidado para eliminar duplicação.
 * Mantém compatibilidade total com o código existente.
 */
@Component
public class AcaoHeaderScraper {
    private static final Logger logger = LoggerFactory.getLogger(AcaoHeaderScraper.class);
    
    private final GenericHeaderScraper genericHeaderScraper;
    
    public AcaoHeaderScraper(GenericHeaderScraper genericHeaderScraper) {
        this.genericHeaderScraper = genericHeaderScraper;
        logger.info("AcaoHeaderScraper inicializado com GenericHeaderScraper consolidado");
    }
    
    /**
     * Extrai informações do cabeçalho da página de uma ação.
     * 
     * REFATORADO: Agora delega para o GenericHeaderScraper consolidado.
     * Mantém a mesma assinatura e comportamento do método original.
     *
     * @param doc O documento HTML da página
     * @return DTO com as informações do cabeçalho
     * @throws ElementNotFoundException se elementos essenciais não forem encontrados
     * @throws IllegalArgumentException se o documento for nulo
     */
    public AcaoInfoHeaderDTO scrapeInfoHeader(Document doc) {
        logger.debug("Delegando extração de cabeçalho de ação para GenericHeaderScraper");
        
        return genericHeaderScraper.scrapeAcaoHeader(doc, AcaoInfoHeaderDTO::new);
    }
}
