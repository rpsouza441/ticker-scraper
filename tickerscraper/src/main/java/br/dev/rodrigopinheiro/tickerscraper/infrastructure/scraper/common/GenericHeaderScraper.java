package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.common;

import br.dev.rodrigopinheiro.tickerscraper.domain.exception.ElementNotFoundException;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.common.dto.HeaderInfoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.validator.ScraperValidator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

/**
 * Scraper genérico consolidado para extração de informações de cabeçalho.
 * Elimina duplicação de código entre AcaoHeaderScraper e FiiHeaderScraper.
 * 
 * ## Padrão de Consolidação Aplicado:
 * 
 * ### 1. Identificação de Duplicatas
 * - Ambos os scrapers tinham lógica idêntica de busca de elementos
 * - Mesma estrutura de fallbacks para seletores CSS
 * - Tratamento de erros e validação duplicados
 * - Apenas diferiam nos seletores específicos e tipo de retorno
 * 
 * ### 2. Técnicas de Refatoração
 * - **Extração de method**: Lógica comum movida para method genérico
 * - **Parametrização**: Seletores CSS passados como parâmetros
 * - **Generics**: Uso de tipos genéricos para flexibilidade de retorno
 * - **Factory Pattern**: Função de criação do DTO como parâmetro
 * 
 * ### 3. Funções Reutilizáveis
 * - `scrapeHeaderInfo()`: method principal genérico
 * - Configuração flexível de seletores por tipo de ativo
 * - Validação centralizada e tratamento de erros
 * 
 * ### 4. Validação da Funcionalidade
 * - Interface comum `HeaderInfoDTO` garante compatibilidade
 * - Mesma assinatura de métodos preservada
 * - Logs detalhados para debugging e monitoramento
 */
@Component
public class GenericHeaderScraper {
    private static final Logger logger = LoggerFactory.getLogger(GenericHeaderScraper.class);
    
    /**
     * Configuração de seletores para diferentes tipos de ativos.
     */
    public static class SelectorConfig {
        private final String[] containerSelectors;
        private final String[] tickerSelectors;
        private final String[] nameSelectors;
        private final String assetType;
        
        public SelectorConfig(String assetType, String[] containerSelectors, 
                            String[] tickerSelectors, String[] nameSelectors) {
            this.assetType = assetType;
            this.containerSelectors = containerSelectors;
            this.tickerSelectors = tickerSelectors;
            this.nameSelectors = nameSelectors;
        }
        
        public String[] getContainerSelectors() { return containerSelectors; }
        public String[] getTickerSelectors() { return tickerSelectors; }
        public String[] getNameSelectors() { return nameSelectors; }
        public String getAssetType() { return assetType; }
    }
    
    // Configurações pré-definidas para diferentes tipos de ativos
    public static final SelectorConfig ACAO_CONFIG = new SelectorConfig(
        "ACAO",
        new String[]{"div.name-ticker", "div.container-header", "header div.company-info"},
        new String[]{"h1", "span.ticker", ".ticker-symbol"},
        new String[]{"h2.name-company", "span.company-name", ".company-title"}
    );
    
    public static final SelectorConfig FII_CONFIG = new SelectorConfig(
        "FII",
        new String[]{"div.name-ticker", "div.container-header", "header div.fii-info"},
        new String[]{"h1", "span.ticker", ".ticker-symbol"},
        new String[]{"h2.name-company", "span.fii-name", ".fii-title"}
    );
    
    /**
     * method genérico para extração de informações de cabeçalho.
     * 
     * @param <T> Tipo do DTO que implementa HeaderInfoDTO
     * @param doc Documento HTML da página
     * @param config Configuração de seletores para o tipo de ativo
     * @param dtoFactory Função para criar o DTO específico (ticker, nome) -> DTO
     * @return DTO com as informações extraídas
     * @throws ElementNotFoundException se elementos essenciais não forem encontrados
     * @throws IllegalArgumentException se o documento for nulo
     */
    public <T extends HeaderInfoDTO> T scrapeHeaderInfo(Document doc, 
                                                        SelectorConfig config,
                                                        BiFunction<String, String, T> dtoFactory) {
        if (doc == null) {
            throw new IllegalArgumentException("Documento HTML não pode ser nulo");
        }
        
        logger.debug("Iniciando extração de informações do cabeçalho para tipo: {}", config.getAssetType());
        
        // Busca o container com fallbacks
        Element container = ScraperValidator.findElementWithFallbacks(doc, config.getContainerSelectors())
                .orElseThrow(() -> {
                    logger.error("Container do cabeçalho não encontrado para tipo: {}", config.getAssetType());
                    return ElementNotFoundException.forSelectors(config.getContainerSelectors());
                });
        
        // Extrai o ticker com fallbacks
        String ticker = ScraperValidator.findElementWithFallbacks(container, config.getTickerSelectors())
                .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                .orElse("N/A");
        
        // Extrai o nome da empresa/fundo com fallbacks
        String nome = ScraperValidator.findElementWithFallbacks(container, config.getNameSelectors())
                .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                .orElse("N/A");
        
        logger.debug("Informações extraídas para {}: ticker={}, nome={}", 
                    config.getAssetType(), ticker, nome);
        
        // Cria o DTO usando a factory fornecida
        return dtoFactory.apply(ticker, nome);
    }
    
    /**
     * method de conveniência para scraping de ações.
     * Mantém compatibilidade com o AcaoHeaderScraper original.
     */
    public <T extends HeaderInfoDTO> T scrapeAcaoHeader(Document doc, 
                                                       BiFunction<String, String, T> dtoFactory) {
        return scrapeHeaderInfo(doc, ACAO_CONFIG, dtoFactory);
    }
    
    /**
     * method de conveniência para scraping de FIIs.
     * Mantém compatibilidade com o FiiHeaderScraper original.
     */
    public <T extends HeaderInfoDTO> T scrapeFiiHeader(Document doc, 
                                                      BiFunction<String, String, T> dtoFactory) {
        return scrapeHeaderInfo(doc, FII_CONFIG, dtoFactory);
    }
}