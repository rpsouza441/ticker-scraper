package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.AcaoRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.ProcessingStatus;
import br.dev.rodrigopinheiro.tickerscraper.application.mapper.RawDataMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.mapper.AcaoScraperMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.fasterxml.jackson.core.JsonProcessingException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AcaoUseCaseService Tests")
class AcaoUseCaseServiceTest {

    @Mock
    private AcaoDataScrapperPort scraperPort;
    
    @Mock
    private AcaoRepositoryPort repositoryPort;
    
    @Mock
    private AcaoScraperMapper scraperMapper;
    
    @Mock
    private RawDataMapper rawDataMapper;
    
    @Mock
    private ObjectMapper objectMapper;
    
    private AcaoUseCaseService acaoUseCaseService;
    
    private static final String TICKER = "PETR4";
    private static final String NORMALIZED_TICKER = "PETR4";
    
    @BeforeEach
    void setUp() {
        acaoUseCaseService = new AcaoUseCaseService(
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
        String tickerLowerCase = "petr4";
        String tickerWithSpaces = " PETR4 ";
        
        // When & Then
        assertThat(acaoUseCaseService.normalize(tickerLowerCase)).isEqualTo("PETR4");
        assertThat(acaoUseCaseService.normalize(tickerWithSpaces)).isEqualTo("PETR4");
        assertThat(acaoUseCaseService.normalize(null)).isNull();
    }
    
    @Test
    @DisplayName("Deve retornar dados do cache quando válido")
    void shouldReturnCachedDataWhenValid() {
        // Given
        Acao cachedAcao = createMockAcao();
        cachedAcao.setDataAtualizacao(LocalDateTime.now().minusHours(12)); // Cache válido
        
        when(repositoryPort.findByTicker(NORMALIZED_TICKER))
            .thenReturn(Optional.of(cachedAcao));
        // Configurar scraper mesmo que não seja usado, para evitar NPE
        when(scraperPort.scrape(anyString()))
            .thenReturn(Mono.just(createMockScrapedData()));
        
        // When
        Mono<Acao> result = acaoUseCaseService.getTickerData(TICKER);
        
        // Then
        StepVerifier.create(result)
            .expectNext(cachedAcao)
            .verifyComplete();
        
        verify(scraperPort, never()).scrape(anyString());
        verify(repositoryPort, never()).save(any(Acao.class), anyString());
    }
    
    @Test
    @DisplayName("Deve fazer scraping quando cache inválido")
    void shouldScrapeWhenCacheInvalid() {
        // Given
        Acao expiredAcao = createMockAcao();
        expiredAcao.setDataAtualizacao(LocalDateTime.now().minusDays(2)); // Cache expirado
        
        AcaoDadosFinanceirosDTO scrapedData = createMockScrapedData();
        Acao newAcao = createMockAcao();
        newAcao.setDataAtualizacao(LocalDateTime.now());
        
        // Configurar mocks para retornar Mono válidos
        when(repositoryPort.findByTicker(NORMALIZED_TICKER))
            .thenReturn(Optional.of(expiredAcao));
        when(scraperPort.scrape(NORMALIZED_TICKER))
            .thenReturn(Mono.just(scrapedData));
        when(scraperMapper.toDomain(scrapedData))
            .thenReturn(newAcao);
        when(repositoryPort.save(any(Acao.class), anyString()))
            .thenReturn(newAcao);
        // Garantir que o ObjectMapper funciona
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        } catch (JsonProcessingException e) {
            // Mock não deve lançar exceção
        }
        
        // When
        Mono<Acao> result = acaoUseCaseService.getTickerData(TICKER);
        
        // Then
        StepVerifier.create(result)
            .expectNext(newAcao)
            .verifyComplete();
        
        verify(scraperPort).scrape(NORMALIZED_TICKER);
        verify(repositoryPort).save(any(Acao.class), anyString());
    }
    
    @Test
    @DisplayName("Deve fazer scraping quando ticker não existe no cache")
    void shouldScrapeWhenTickerNotInCache() {
        // Given
        AcaoDadosFinanceirosDTO scrapedData = createMockScrapedData();
        Acao newAcao = createMockAcao();
        
        // Configurar mocks para retornar Mono válidos
        when(repositoryPort.findByTicker(NORMALIZED_TICKER))
            .thenReturn(Optional.empty());
        when(scraperPort.scrape(NORMALIZED_TICKER))
            .thenReturn(Mono.just(scrapedData));
        when(scraperMapper.toDomain(scrapedData))
            .thenReturn(newAcao);
        when(repositoryPort.save(any(Acao.class), anyString()))
            .thenReturn(newAcao);
        // Garantir que o ObjectMapper funciona
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        } catch (JsonProcessingException e) {
            // Mock não deve lançar exceção
        }
        
        // When
        Mono<Acao> result = acaoUseCaseService.getTickerData(TICKER);
        
        // Then
        StepVerifier.create(result)
            .expectNext(newAcao)
            .verifyComplete();
        
        verify(scraperPort).scrape(NORMALIZED_TICKER);
        verify(repositoryPort).save(any(Acao.class), anyString());
    }
    
    @Test
    @DisplayName("Deve propagar erro quando scraping falha")
    void shouldPropagateErrorWhenScrapingFails() {
        // Given
        RuntimeException scrapingError = new RuntimeException("Scraping failed");
        
        when(repositoryPort.findByTicker(NORMALIZED_TICKER))
            .thenReturn(Optional.empty());
        when(scraperPort.scrape(NORMALIZED_TICKER))
            .thenReturn(Mono.error(scrapingError));
        
        // When
        Mono<Acao> result = acaoUseCaseService.getTickerData(TICKER);
        
        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
        
        verify(repositoryPort, never()).save(any(Acao.class), anyString());
    }
    
    @Test
    @DisplayName("Deve retornar dados brutos como response")
    void shouldReturnRawDataAsResponse() {
        // Given
        AcaoDadosFinanceirosDTO rawData = createMockScrapedData();
        AcaoRawDataResponse expectedResponse = createMockRawResponse();
        AcaoRawDataResponse failedResponse = createMockFailedResponse();
        
        when(repositoryPort.findByTicker(NORMALIZED_TICKER))
            .thenReturn(Optional.empty());
        when(scraperPort.scrape(NORMALIZED_TICKER))
            .thenReturn(Mono.just(rawData));
        when(rawDataMapper.toAcaoRawDataResponse(rawData))
            .thenReturn(expectedResponse);
        when(rawDataMapper.createFailedAcaoResponse(eq(NORMALIZED_TICKER), eq("SCRAPER"), anyString()))
            .thenReturn(failedResponse);
        
        // Simular persistência
        Acao savedAcao = createMockAcao();
        when(scraperMapper.toDomain(rawData)).thenReturn(savedAcao);
        when(repositoryPort.save(any(Acao.class), anyString())).thenReturn(savedAcao);
        
        // When
        Mono<AcaoRawDataResponse> result = acaoUseCaseService.getRawTickerData(TICKER);
        
        // Then
        StepVerifier.create(result)
            .expectNext(expectedResponse)
            .verifyComplete();
    }
    
    @Test
    @DisplayName("Deve validar cache corretamente")
    void shouldValidateCacheCorrectly() {
        // Given
        Acao recentAcao = createMockAcao();
        recentAcao.setDataAtualizacao(LocalDateTime.now().minusHours(12));
        
        Acao oldAcao = createMockAcao();
        oldAcao.setDataAtualizacao(LocalDateTime.now().minusDays(2));
        
        // When & Then
        assertThat(acaoUseCaseService.isCacheValid(recentAcao, java.time.Duration.ofDays(1)))
            .isTrue();
        assertThat(acaoUseCaseService.isCacheValid(oldAcao, java.time.Duration.ofDays(1)))
            .isFalse();
    }
    
    @Test
    @DisplayName("Deve converter DTO para domínio")
    void shouldConvertDtoToDomain() {
        // Given
        AcaoDadosFinanceirosDTO dto = createMockScrapedData();
        Acao expectedAcao = createMockAcao();
        
        when(scraperMapper.toDomain(dto)).thenReturn(expectedAcao);
        
        // When
        Acao result = acaoUseCaseService.toDomain(dto);
        
        // Then
        assertThat(result).isEqualTo(expectedAcao);
        verify(scraperMapper).toDomain(dto);
    }
    
    @Test
    @DisplayName("Deve salvar domínio corretamente")
    void shouldSaveDomainCorrectly() {
        // Given
        Acao domain = createMockAcao();
        AcaoDadosFinanceirosDTO raw = createMockScrapedData();
        Acao savedAcao = createMockAcao();
        
        // Configurar ObjectMapper mock para não falhar na serialização
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");
        } catch (Exception e) {
            // Mock não deve lançar exceção
        }
        
        // Garantir que o mock retorna um objeto válido
        when(repositoryPort.save(any(Acao.class), anyString())).thenReturn(savedAcao);
        
        // When
        Acao result = acaoUseCaseService.saveDomain(domain, raw);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(savedAcao);
        assertThat(result.getTicker()).isEqualTo(savedAcao.getTicker());
        verify(repositoryPort).save(eq(domain), anyString());
    }
    
    // Helper methods para criar objetos mock
    
    private Acao createMockAcao() {
        Acao acao = new Acao();
        acao.setTicker(TICKER);
        acao.setDataAtualizacao(LocalDateTime.now());
        return acao;
    }
    
    private AcaoDadosFinanceirosDTO createMockScrapedData() {
        AcaoInfoHeaderDTO header = new AcaoInfoHeaderDTO(
            TICKER,
            "Petróleo Brasileiro S.A."
        );
        
        AcaoInfoCardsDTO cards = new AcaoInfoCardsDTO(
            "Valor de Mercado: R$ 150B",
            "Patrimônio Líquido: R$ 25.5B"
        );
        
        AcaoInfoDetailedDTO detailed = new AcaoInfoDetailedDTO(
            "ON", "Petróleo e Gás", "Grande", "B3", "PETR4", "Ordinária", 
            "Ativa", "Brasil", "Energia", "Petróleo", "Exploração", "Bovespa", 
            "Novo Mercado", "Alta", "Sim"
        );
        
        Map<String, br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoIndicadorFundamentalistaDTO> indicatorsMap = new HashMap<>();
        AcaoIndicadoresFundamentalistasDTO indicators = new AcaoIndicadoresFundamentalistasDTO(indicatorsMap);
        
        return new AcaoDadosFinanceirosDTO(header, detailed, cards, indicators);
    }
    
    private AcaoRawDataResponse createMockRawResponse() {
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("ticker", TICKER);
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("source", "SCRAPER");
        
        return new AcaoRawDataResponse(
            TICKER,
            rawData,
            "SCRAPER",
            LocalDateTime.now(),
            ProcessingStatus.SUCCESS,
            metadata
        );
    }
    
    private AcaoRawDataResponse createMockFailedResponse() {
        Map<String, Object> rawData = new HashMap<>();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("error", "Falha na coleta de dados");
        
        return new AcaoRawDataResponse(
            TICKER,
            rawData,
            "SCRAPER",
            LocalDateTime.now(),
            ProcessingStatus.FAILED,
            metadata
        );
    }
}