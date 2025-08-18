package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.validator;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

/**
 * Classe utilitária para validação e extração segura de elementos durante o scraping.
 * Fornece métodos para buscar elementos com fallbacks e extrair texto com validação.
 */
public class ScraperValidator {
    private static final Logger logger = LoggerFactory.getLogger(ScraperValidator.class);
    
    /**
     * Busca um elemento usando uma lista de seletores CSS como fallbacks.
     * Se o primeiro seletor não encontrar o elemento, tenta os próximos na sequência.
     *
     * @param parent O elemento pai onde a busca será realizada
     * @param selectors Array de seletores CSS para tentar em sequência
     * @return Optional contendo o elemento encontrado ou empty se nenhum seletor funcionar
     */
    public static Optional<Element> findElementWithFallbacks(Element parent, String... selectors) {
        if (parent == null || selectors == null || selectors.length == 0) {
            return Optional.empty();
        }
        
        for (String selector : selectors) {
            Elements elements = parent.select(selector);
            if (!elements.isEmpty()) {
                logger.debug("Elemento encontrado com seletor: {}", selector);
                return Optional.of(elements.first());
            }
        }
        
        logger.warn("Nenhum elemento encontrado com os seletores: {}", Arrays.toString(selectors));
        return Optional.empty();
    }
    
    /**
     * Extrai texto de um elemento com validação.
     * Tenta extrair o texto usando diferentes métodos (text, ownText, attr) em sequência.
     *
     * @param element O elemento de onde extrair o texto
     * @param attributeName Nome do atributo para tentar extrair (opcional)
     * @return Optional contendo o texto extraído ou empty se não for possível extrair
     */
    public static Optional<String> extractTextWithValidation(Element element, String... attributeName) {
        if (element == null) {
            return Optional.empty();
        }
        
        // Tenta obter o texto do elemento
        String text = element.text();
        if (text != null && !text.isBlank()) {
            return Optional.of(text.trim());
        }
        
        // Tenta obter apenas o texto próprio do elemento (sem filhos)
        String ownText = element.ownText();
        if (ownText != null && !ownText.isBlank()) {
            return Optional.of(ownText.trim());
        }
        
        // Se fornecido, tenta obter o valor de um atributo específico
        if (attributeName != null && attributeName.length > 0) {
            for (String attr : attributeName) {
                String attrValue = element.attr(attr);
                if (attrValue != null && !attrValue.isBlank()) {
                    return Optional.of(attrValue.trim());
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Extrai texto de um elemento com validação, usando um valor padrão se a extração falhar.
     *
     * @param parent O elemento pai onde a busca será realizada
     * @param selector O seletor CSS para encontrar o elemento
     * @param defaultValue Valor padrão a ser retornado se a extração falhar
     * @return Optional contendo o texto extraído ou o valor padrão
     */
    public static Optional<String> extractTextWithDefault(Element parent, String selector, String defaultValue) {
        if (parent == null || selector == null) {
            return Optional.of(defaultValue);
        }
        
        Element element = parent.selectFirst(selector);
        if (element == null) {
            return Optional.of(defaultValue);
        }
        
        String text = element.text();
        return Optional.of(text != null && !text.isBlank() ? text.trim() : defaultValue);
    }
}