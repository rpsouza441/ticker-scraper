package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.FiiRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.ProcessingStatus;
import br.dev.rodrigopinheiro.tickerscraper.application.mapper.RawDataMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.FundoImobiliario;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.mapper.FiiScraperMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.fasterxml.jackson.core.JsonProcessingException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FiiUseCaseService Tests")
class FiiUseCaseServiceTest {

    @Mock
    private FiiDataScrapperPort scraperPort;
    
    @Mock
    private FiiRepositoryPort repositoryPort;
    
    @Mock
    private FiiScraperMapper scraperMapper;
    
    @Mock
    private RawDataMapper rawDataMapper;
    
    @Mock
    private ObjectMapper objectMapper;
    
    private FiiUseCaseService fiiUseCaseService;
    
    private static final String TICKER = "HGLG11";
    private static final String NORMALIZED_TICKER = "HGLG11";
    
    @BeforeEach
    void setUp() {
        fiiUseCaseService = new FiiUseCaseService(
            scraperPort,
            repositoryPort,
            scraperMapper,
            rawDataMapper,
            objectMapper
        );
    }
    
    @Test
    @DisplayName("Deve normalizar ticker corretamente")
    void shouldNormalizeTicker() {
        // Given
        String tickerLowerCase = "hglg11";
        String tickerWithSpaces = " HGLG11 ";
        
        // When & Then
        assertThat(fiiUseCaseService.normalize(tickerLowerCase)).isEqualTo("HGLG11");
        assertThat(fiiUseCaseService.normalize(tickerWithSpaces)).isEqualTo("HGLG11");
        assertThat(fiiUseCaseService.normalize(null)).isNull();
    }
    
    @Test
    @DisplayName("Deve retornar dados do cache quando válido")
    void shouldReturnCachedDataWhenValid() {
        // Given
        FundoImobiliario cachedFii = createMockFii();
        cachedFii.setDataAtualizacao(LocalDateTime.now().minusHours(12)); // Cache válido
        
        // Usar o método correto que é chamado pelo FiiUseCaseService
        when(repositoryPort.findByTickerWithDividendos(NORMALIZED_TICKER))
            .thenReturn(Optional.of(cachedFii));
        // Configurar scraper mesmo que não seja usado, para evitar NPE
        when(scraperPort.scrape(anyString()))
            .thenReturn(Mono.just(createMockScrapedData()));
        
        // When
        Mono<FundoImobiliario> result = fiiUseCaseService.getTickerData(TICKER);
        
        // Then
        StepVerifier.create(result)
            .expectNext(cachedFii)
            .verifyComplete();
        
        verify(scraperPort, never()).scrape(anyString());
        verify(repositoryPort, never()).saveReplacingDividends(any(FundoImobiliario.class), any(), anyString());
    }
    
    @Test
    @DisplayName("Deve fazer scraping quando cache inválido")
    void shouldScrapeWhenCacheInvalid() {
        // Given
        FundoImobiliario expiredFii = createMockFii();
        expiredFii.setDataAtualizacao(LocalDateTime.now().minusDays(2)); // Cache expirado
        
        FiiDadosFinanceirosDTO scrapedData = createMockScrapedData();
        FundoImobiliario newFii = createMockFii();
        newFii.setDataAtualizacao(LocalDateTime.now());
        
        // Configurar todos os mocks para retornar valores válidos
        when(repositoryPort.findByTickerWithDividendos(NORMALIZED_TICKER))
            .thenReturn(Optional.of(expiredFii));
        when(scraperPort.scrape(NORMALIZED_TICKER))
            .thenReturn(Mono.just(scrapedData));
        when(scraperMapper.toDomain(scrapedData))
            .thenReturn(newFii);
        when(repositoryPort.saveReplacingDividends(any(FundoImobiliario.class), any(), anyString()))
            .thenReturn(newFii);
        // Garantir que o ObjectMapper funciona
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        } catch (JsonProcessingException e) {
            // Mock não deve lançar exceção
        }
        
        // When
        Mono<FundoImobiliario> result = fiiUseCaseService.getTickerData(TICKER);
        
        // Then
        StepVerifier.create(result)
            .expectNext(newFii)
            .verifyComplete();
        
        verify(scraperPort).scrape(NORMALIZED_TICKER);
        verify(repositoryPort).saveReplacingDividends(any(FundoImobiliario.class), any(), anyString());
    }
    
    @Test
    @DisplayName("Deve fazer scraping quando ticker não existe no cache")
    void shouldScrapeWhenTickerNotInCache() {
        // Given
        FiiDadosFinanceirosDTO scrapedData = createMockScrapedData();
        FundoImobiliario newFii = createMockFii();
        
        // Configurar todos os mocks para retornar valores válidos
        when(repositoryPort.findByTickerWithDividendos(NORMALIZED_TICKER))
            .thenReturn(Optional.empty());
        when(scraperPort.scrape(NORMALIZED_TICKER))
            .thenReturn(Mono.just(scrapedData));
        when(scraperMapper.toDomain(scrapedData))
            .thenReturn(newFii);
        when(repositoryPort.saveReplacingDividends(any(FundoImobiliario.class), any(), anyString()))
            .thenReturn(newFii);
        // Garantir que o ObjectMapper funciona
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        } catch (JsonProcessingException e) {
            // Mock não deve lançar exceção
        }
        
        // When
        Mono<FundoImobiliario> result = fiiUseCaseService.getTickerData(TICKER);
        
        // Then
        StepVerifier.create(result)
            .expectNext(newFii)
            .verifyComplete();
        
        verify(scraperPort).scrape(NORMALIZED_TICKER);
        verify(repositoryPort).saveReplacingDividends(any(FundoImobiliario.class), any(), anyString());
    }
    
    @Test
    @DisplayName("Deve propagar erro quando scraping falha")
    void shouldPropagateErrorWhenScrapingFails() {
        // Given
        RuntimeException scrapingError = new RuntimeException("FII scraping failed");
        
        when(repositoryPort.findByTickerWithDividendos(NORMALIZED_TICKER))
            .thenReturn(Optional.empty());
        when(scraperPort.scrape(NORMALIZED_TICKER))
            .thenReturn(Mono.error(scrapingError));
        
        // When
        Mono<FundoImobiliario> result = fiiUseCaseService.getTickerData(TICKER);
        
        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
    }
    
    @Test
    @DisplayName("Deve retornar dados brutos como response")
    void shouldReturnRawDataAsResponse() {
        // Given
        FiiDadosFinanceirosDTO rawData = createMockScrapedData();
        FiiRawDataResponse expectedResponse = createMockRawResponse();
        
        when(repositoryPort.findByTicker(NORMALIZED_TICKER))
            .thenReturn(Optional.empty());
        when(scraperPort.scrape(NORMALIZED_TICKER))
            .thenReturn(Mono.just(rawData));
        when(rawDataMapper.toFiiRawDataResponse(eq(rawData), any()))
            .thenReturn(expectedResponse);
        when(rawDataMapper.createFailedFiiResponse(eq(NORMALIZED_TICKER), eq("SCRAPER"), anyString()))
            .thenReturn(createMockFailedResponse());
        
        // Simular persistência
        FundoImobiliario savedFii = createMockFii();
        when(scraperMapper.toDomain(rawData)).thenReturn(savedFii);
        when(repositoryPort.saveReplacingDividends(any(FundoImobiliario.class), any(), anyString())).thenReturn(savedFii);
        
        // When
        Mono<FiiRawDataResponse> result = fiiUseCaseService.getRawTickerData(TICKER);
        
        // Then
        StepVerifier.create(result)
            .expectNext(expectedResponse)
            .verifyComplete();
    }
    
    @Test
    @DisplayName("Deve validar cache corretamente")
    void shouldValidateCacheCorrectly() {
        // Given
        FundoImobiliario recentFii = createMockFii();
        recentFii.setDataAtualizacao(LocalDateTime.now().minusHours(12));
        
        FundoImobiliario oldFii = createMockFii();
        oldFii.setDataAtualizacao(LocalDateTime.now().minusDays(2));
        
        // When & Then
        assertThat(fiiUseCaseService.isCacheValid(recentFii, java.time.Duration.ofDays(1)))
            .isTrue();
        assertThat(fiiUseCaseService.isCacheValid(oldFii, java.time.Duration.ofDays(1)))
            .isFalse();
    }
    
    @Test
    @DisplayName("Deve converter DTO para domínio")
    void shouldConvertDtoToDomain() {
        // Given
        FiiDadosFinanceirosDTO dto = createMockScrapedData();
        FundoImobiliario expectedFii = createMockFii();
        
        when(scraperMapper.toDomain(dto)).thenReturn(expectedFii);
        
        // When
        FundoImobiliario result = fiiUseCaseService.toDomain(dto);
        
        // Then
        assertThat(result).isEqualTo(expectedFii);
        verify(scraperMapper).toDomain(dto);
    }
    
    @Test
    @DisplayName("Deve salvar domínio corretamente")
    void shouldSaveDomainCorrectly() {
        // Given
        FundoImobiliario domain = createMockFii();
        FiiDadosFinanceirosDTO raw = createMockScrapedData();
        FundoImobiliario savedFii = createMockFii();
        
        // Garantir que o mock retorna um objeto válido
        when(repositoryPort.saveReplacingDividends(eq(domain), any(), anyString())).thenReturn(savedFii);
        
        // When
        FundoImobiliario result = fiiUseCaseService.saveDomain(domain, raw);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(savedFii);
        verify(repositoryPort).saveReplacingDividends(eq(domain), any(), anyString());
    }
    
    // Helper methods para criar objetos mock
    
    private FundoImobiliario createMockFii() {
        FundoImobiliario fii = new FundoImobiliario();
        fii.setTicker(TICKER);
        fii.setDataAtualizacao(LocalDateTime.now());
        return fii;
    }
    
    private FiiDadosFinanceirosDTO createMockScrapedData() {
        FiiInfoHeaderDTO header = new FiiInfoHeaderDTO(
            TICKER,
            "CSHG LOGÍSTICA FII"
        );
        
        FiiInfoCardsDTO cards = new FiiInfoCardsDTO(
            "Patrimônio Líquido: R$ 1.5B",
            "Dividend Yield: 10.5%"
        );
        
        FiiInfoSobreDTO sobre = new FiiInfoSobreDTO(
            "Logística", "CSHG Logística", "Fundo focado em logística", "Ativo", "Brasil",
            "Logística", "Galpões", "São Paulo", "Administrador XYZ", "Gestora ABC",
            "Auditoria DEF", "Banco GHI", "Consultoria JKL", "Avaliação MNO", "Outros PQR"
        );
        
        FiiCotacaoDTO cotacao = new FiiCotacaoDTO(
            new BigDecimal("95.50"),
            LocalDateTime.now()
        );
        
        List<FiiDividendoDTO> dividendos = Arrays.asList(
            new FiiDividendoDTO(
                new BigDecimal("0.85"),
                java.time.YearMonth.now().minusMonths(1)
            )
        );
        
        Map<String, List<br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiIndicadorHistoricoItemDTO>> historico = new HashMap<>();
        FiiIndicadorHistoricoDTO indicadorHistorico = new FiiIndicadorHistoricoDTO(historico);
        
        return new FiiDadosFinanceirosDTO(
            Integer.valueOf(1), // internalId não pode ser null
            header,
            indicadorHistorico,
            sobre,
            cards,
            dividendos,
            cotacao
        );
    }
    
    private FiiRawDataResponse createMockRawResponse() {
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("ticker", TICKER);
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("source", "SCRAPER");
        
        Map<String, String> apiUrls = new HashMap<>();
        apiUrls.put("cotacao", "http://api.example.com/cotacao");
        
        return new FiiRawDataResponse(
            TICKER,
            rawData,
            "SCRAPER",
            LocalDateTime.now(),
            ProcessingStatus.SUCCESS,
            metadata,
            apiUrls
        );
    }
    
    private FiiRawDataResponse createMockFailedResponse() {
        Map<String, Object> rawData = new HashMap<>();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("error", "Falha na coleta de dados");
        
        Map<String, String> apiUrls = new HashMap<>();
        
        return new FiiRawDataResponse(
            TICKER,
            rawData,
            "SCRAPER",
            LocalDateTime.now(),
            ProcessingStatus.FAILED,
            metadata,
            apiUrls
        );
    }
}