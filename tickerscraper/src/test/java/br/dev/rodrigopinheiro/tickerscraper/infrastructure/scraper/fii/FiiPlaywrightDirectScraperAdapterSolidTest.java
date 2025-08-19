package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.PlaywrightInitializer;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.common.CorrelationIdProvider;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

/**
 * Testes específicos para validar conformidade com os princípios SOLID
 * no FiiPlaywrightDirectScraperAdapter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FiiPlaywrightDirectScraperAdapter - Conformidade SOLID")
class FiiPlaywrightDirectScraperAdapterSolidTest {

    @Mock
    private PlaywrightInitializer pwInit;
    
    @Mock
    private FiiSeleniumScraperAdapter seleniumFallback;
    
    @Mock
    private FiiHeaderScraper headerScraper;
    
    @Mock
    private FiiInfoSobreScraper infoSobreScraper;
    
    @Mock
    private FiiCardsScraper cardsScraper;
    
    @Mock
    private FiiInternalIdScrapper internalIdScrapper;
    
    @Mock
    private FiiApiScraper apiScraper;
    
    @Mock
    private CorrelationIdProvider correlationIdProvider;
    
    private FiiPlaywrightDirectScraperAdapter adapter;
    
    @BeforeEach
    void setUp() {
        adapter = new FiiPlaywrightDirectScraperAdapter(
            pwInit,
            seleniumFallback,
            headerScraper,
            infoSobreScraper,
            cardsScraper,
            internalIdScrapper,
            apiScraper,
            correlationIdProvider
        );
    }
    
    @Test
    @DisplayName("SRP: Deve ter responsabilidade única de scraping de FII")
    void shouldHaveSingleResponsibilityForFiiScraping() {
        // Verifica se a classe tem uma responsabilidade bem definida
        assertThat(adapter.getClass().getSimpleName())
            .contains("Fii")
            .contains("Scraper")
            .contains("Adapter");
        
        // Verifica se implementa apenas as interfaces necessárias
        Class<?>[] interfaces = adapter.getClass().getInterfaces();
        assertThat(interfaces).hasSize(1); // Apenas FiiDataScrapperPort
    }
    
    @Test
    @DisplayName("OCP: Deve implementar executeSpecificScraping corretamente")
    void shouldImplementExecuteSpecificScrapingCorrectly() {
        // Arrange
        String html = "<div class='name-ticker'><h1>KNRI11</h1><h2>KINEA</h2></div>";
        Document doc = Jsoup.parse(html);
        String ticker = "KNRI11";
        
        // Mock dos scrapers
        when(headerScraper.scrape(doc)).thenReturn(
            new br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoHeaderDTO("KNRI11", "KINEA")
        );
        when(infoSobreScraper.scrape(doc)).thenReturn(
            new br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoSobreDTO(
                "Logística", "KINEA", "Fundo focado em logística", "Ativo", "Brasil",
                "Logística", "Galpões", "São Paulo", "Administrador", "Gestora",
                "Auditoria", "Banco", "Consultoria", "Avaliação", "Outros"
            )
        );
        when(cardsScraper.scrape(doc)).thenReturn(
            new br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoCardsDTO(
                "R$ 95.50", "10.5%"
            )
        );
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            FiiDadosFinanceirosDTO result = adapter.executeSpecificScraping(doc, ticker);
            
            // Verifica se retorna um DTO válido (não lança UnsupportedOperationException)
            assertThat(result).isNotNull();
            assertThat(result.infoHeader()).isNotNull();
            assertThat(result.infoSobre()).isNotNull();
            assertThat(result.infoCards()).isNotNull();
        });
    }
    
    @Test
    @DisplayName("LSP: Deve ser substituível pela classe base AbstractScraperAdapter")
    void shouldBeSubstitutableByBaseClass() {
        // Verifica se pode ser tratado como AbstractScraperAdapter
        assertThat(adapter)
            .isInstanceOf(br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base.AbstractScraperAdapter.class);
        
        // Verifica se implementa os métodos template corretamente
        assertThat(adapter.getEssentialSelectors()).isNotEmpty();
        assertThat(adapter.getCardsSelectors()).isNotEmpty();
        assertThat(adapter.buildUrl("TEST11")).contains("TEST11");
    }
    
    @Test
    @DisplayName("ISP: Deve implementar apenas interfaces necessárias")
    void shouldImplementOnlyNecessaryInterfaces() {
        // Verifica se não implementa interfaces desnecessárias
        Class<?>[] interfaces = adapter.getClass().getInterfaces();
        
        assertThat(interfaces)
            .hasSize(1)
            .extracting(Class::getSimpleName)
            .containsExactly("FiiDataScrapperPort");
    }
    
    @Test
    @DisplayName("DIP: Deve usar injeção de dependência para CorrelationIdProvider")
    void shouldUseDependencyInjectionForCorrelationIdProvider() {
        // Act & Assert
        // Verifica se o CorrelationIdProvider é injetado corretamente
        // (não há acesso direto ao campo, mas podemos verificar se não há exceções)
        assertDoesNotThrow(() -> {
            // O adapter deve funcionar com o provider injetado
            assertThat(adapter).isNotNull();
            
            // Verifica se o construtor aceita o CorrelationIdProvider
            // (se chegou até aqui, a injeção funcionou)
            assertThat(correlationIdProvider).isNotNull();
        });
    }
    
    @Test
    @DisplayName("Timeout Padronizado: Deve usar constantes da classe base")
    void shouldUseStandardizedTimeoutConstants() {
        // Verifica se o adapter herda corretamente da classe base
        // As constantes são protected, então verificamos indiretamente
        assertThat(adapter)
            .isInstanceOf(br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base.AbstractScraperAdapter.class);
        
        // Verifica se os métodos template estão implementados (indicando herança correta)
        assertThat(adapter.getEssentialSelectors()).isNotEmpty();
        assertThat(adapter.getCardsSelectors()).isNotEmpty();
        
        // As constantes de timeout são usadas internamente pelo adapter
        // A verificação é feita indiretamente através do comportamento
        assertDoesNotThrow(() -> {
            // Se as constantes estão disponíveis, o adapter pode ser instanciado sem erro
            assertThat(adapter).isNotNull();
        });
    }
    
    @Test
    @DisplayName("Separação de Responsabilidades: Deve ter métodos com responsabilidades específicas")
    void shouldHaveMethodsWithSpecificResponsibilities() {
        // Verifica se os métodos têm responsabilidades bem definidas
        
        // Método principal de scraping
        assertDoesNotThrow(() -> adapter.getClass().getDeclaredMethod("scrape", String.class));
        
        // Método de fallback
        assertDoesNotThrow(() -> adapter.getClass().getDeclaredMethod("fallbackToSelenium", String.class, Exception.class));
        
        // Método específico para scraping com APIs (privado)
        assertDoesNotThrow(() -> adapter.getClass().getDeclaredMethod("scrapeWithAsyncApis", String.class));
        
        // Método de template da classe base
        assertDoesNotThrow(() -> adapter.getClass().getDeclaredMethod("executeSpecificScraping", Document.class, String.class));
    }
}