package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.validator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe ScraperValidator.
 */
class ScraperValidatorTest {

    @Test
    @DisplayName("Deve encontrar elemento com o primeiro seletor válido")
    void findElementWithFirstValidSelector() {
        // Arrange
        Document doc = Jsoup.parse("<div><span class='test'>Conteúdo</span></div>");
        
        // Act
        Optional<Element> result = ScraperValidator.findElementWithFallbacks(doc, ".test", "#notexist");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Conteúdo", result.get().text());
    }
    
    @Test
    @DisplayName("Deve encontrar elemento com o segundo seletor quando o primeiro falha")
    void findElementWithSecondSelector() {
        // Arrange
        Document doc = Jsoup.parse("<div><span id='second'>Conteúdo Alternativo</span></div>");
        
        // Act
        Optional<Element> result = ScraperValidator.findElementWithFallbacks(doc, ".notexist", "#second");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Conteúdo Alternativo", result.get().text());
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando nenhum seletor encontra elementos")
    void returnEmptyWhenNoSelectorsMatch() {
        // Arrange
        Document doc = Jsoup.parse("<div><p>Texto</p></div>");
        
        // Act
        Optional<Element> result = ScraperValidator.findElementWithFallbacks(doc, ".notexist", "#notexist");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("Deve extrair texto com validação usando text()")
    void extractTextWithValidationUsingText() {
        // Arrange
        Element element = Jsoup.parse("<div>Texto Válido</div>").selectFirst("div");
        
        // Act
        Optional<String> result = ScraperValidator.extractTextWithValidation(element);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Texto Válido", result.get());
    }
    
    @Test
    @DisplayName("Deve extrair texto com validação usando ownText()")
    void extractTextWithValidationUsingOwnText() {
        // Arrange
        Element element = Jsoup.parse("<div>Texto Próprio <span>Filho</span></div>").selectFirst("div");
        
        // Act - O método extractTextWithValidation sempre tenta text() primeiro, depois ownText()
        Optional<String> result = ScraperValidator.extractTextWithValidation(element);
        
        // Assert - O resultado será o text() completo, não apenas ownText()
        assertTrue(result.isPresent());
        assertEquals("Texto Próprio Filho", result.get());
    }
    
    @Test
    @DisplayName("Deve extrair texto de atributo quando especificado")
    void extractTextFromAttribute() {
        // Arrange - Elemento sem texto para forçar extração do atributo
        Element element = Jsoup.parse("<div data-value='Valor do Atributo'></div>").selectFirst("div");
        
        // Act
        Optional<String> result = ScraperValidator.extractTextWithValidation(element, "data-value");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Valor do Atributo", result.get());
    }
    
    @Test
    @DisplayName("Deve extrair texto com valor padrão quando elemento existe")
    void extractTextWithDefaultWhenElementExists() {
        // Arrange
        Document doc = Jsoup.parse("<div><span class='target'>Valor Encontrado</span></div>");
        
        // Act
        Optional<String> result = ScraperValidator.extractTextWithDefault(doc, ".target", "Valor Padrão");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Valor Encontrado", result.get());
    }
    
    @Test
    @DisplayName("Deve retornar valor padrão quando elemento não existe")
    void returnDefaultValueWhenElementNotFound() {
        // Arrange
        Document doc = Jsoup.parse("<div><p>Outro conteúdo</p></div>");
        
        // Act
        Optional<String> result = ScraperValidator.extractTextWithDefault(doc, ".notexist", "Valor Padrão");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Valor Padrão", result.get());
    }
    
    @Test
    @DisplayName("Deve lidar com elemento nulo no findElementWithFallbacks")
    void handleNullElementInFindElementWithFallbacks() {
        // Act
        Optional<Element> result = ScraperValidator.findElementWithFallbacks(null, ".test");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("Deve lidar com seletores nulos no findElementWithFallbacks")
    void handleNullSelectorsInFindElementWithFallbacks() {
        // Arrange
        Document doc = Jsoup.parse("<div><span>Conteúdo</span></div>");
        
        // Act
        Optional<Element> result = ScraperValidator.findElementWithFallbacks(doc, (String[]) null);
        
        // Assert
        assertFalse(result.isPresent());
    }
}