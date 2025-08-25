package br.dev.rodrigopinheiro.tickerscraper.application.mapper;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.AcaoRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.FiiRawDataResponse;
import br.dev.rodrigopinheiro.tickerscraper.application.dto.ProcessingStatus;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapper MapStruct para conversão de DTOs entre camadas de infraestrutura e application.
 * 
 * <p>Este mapper segue os princípios da Arquitetura Hexagonal, convertendo DTOs de infraestrutura
 * (dados brutos de scraping) para DTOs da camada application (contratos de caso de uso).</p>
 * 
 * <p>Utiliza MapStruct para geração de código em tempo de compilação, garantindo:
 * <ul>
 *   <li>Performance otimizada (sem reflection)</li>
 *   <li>Type safety em tempo de compilação</li>
 *   <li>Código gerado inspecionável para debugging</li>
 *   <li>Integração automática com Spring Framework</li>
 * </ul></p>
 * 
 * @author Sistema de Scraping
 * @since 1.0
 * @see org.mapstruct.Mapper
 * @see br.dev.rodrigopinheiro.tickerscraper.application.dto.AcaoRawDataResponse
 * @see br.dev.rodrigopinheiro.tickerscraper.application.dto.FiiRawDataResponse
 */
@Mapper(componentModel = "spring")
public interface RawDataMapper {
    
    /**
     * Converte DTO de infraestrutura de Ação para DTO da camada application.
     * 
     * <p>Utiliza mapeamento declarativo do MapStruct com métodos auxiliares qualificados
     * para lógica de conversão complexa. O mapeamento inclui:</p>
     * <ul>
     *   <li>Extração do ticker do header com valor padrão</li>
     *   <li>Timestamp automático de scraping</li>
     *   <li>Determinação automática do status de processamento</li>
     *   <li>Construção de mapa de dados brutos estruturado</li>
     *   <li>Metadados de completude dos dados</li>
     * </ul>
     * 
     * @param infraDto DTO de infraestrutura com dados coletados do scraping
     * @return DTO da application com dados estruturados e metadados
     */
    @Mapping(target = "ticker", source = "infoHeader.ticker", defaultValue = "UNKNOWN")
    @Mapping(target = "source", constant = "PLAYWRIGHT_SCRAPER")
    @Mapping(target = "scrapingTimestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "processingStatus", source = ".", qualifiedByName = "determineAcaoProcessingStatus")
    @Mapping(target = "rawData", source = ".", qualifiedByName = "buildAcaoRawDataMap")
    @Mapping(target = "metadata", source = ".", qualifiedByName = "buildAcaoMetadata")
    AcaoRawDataResponse toAcaoRawDataResponse(AcaoDadosFinanceirosDTO infraDto);
    
    /**
     * Constrói mapa de dados brutos para Ação a partir do DTO de infraestrutura.
     * 
     * <p>Método auxiliar do MapStruct que extrai e organiza dados de diferentes
     * seções do DTO de infraestrutura em um mapa chave-valor estruturado.</p>
     * 
     * @param infraDto DTO de infraestrutura da ação
     * @return Mapa com dados brutos organizados por categoria
     */
    @Named("buildAcaoRawDataMap")
    default Map<String, Object> buildAcaoRawDataMap(AcaoDadosFinanceirosDTO infraDto) {
        Map<String, Object> rawData = Collections.synchronizedMap(new HashMap<>());
        
        if (infraDto == null) return rawData;
        
        // Converter dados do header
        if (infraDto.infoHeader() != null) {
            rawData.put("ticker", infraDto.infoHeader().ticker());
            rawData.put("nomeEmpresa", infraDto.infoHeader().nomeEmpresa());
        }
        
        // Converter dados dos cards
        if (infraDto.infoCards() != null) {
            rawData.put("cotacao", infraDto.infoCards().cotacao());
            rawData.put("variacao12M", infraDto.infoCards().variacao12M());
        }
        
        // Converter dados detalhados
        if (infraDto.infoDetailed() != null) {
            rawData.put("valorMercado", infraDto.infoDetailed().valorMercado());
            rawData.put("valorFirma", infraDto.infoDetailed().valorFirma());
            rawData.put("patrimonioLiquido", infraDto.infoDetailed().patrimonioLiquido());
            rawData.put("numeroTotalPapeis", infraDto.infoDetailed().numeroTotalPapeis());
            rawData.put("ativos", infraDto.infoDetailed().ativos());
            rawData.put("ativoCirculante", infraDto.infoDetailed().ativoCirculante());
            rawData.put("dividaBruta", infraDto.infoDetailed().dividaBruta());
            rawData.put("dividaLiquida", infraDto.infoDetailed().dividaLiquida());
            rawData.put("disponibilidade", infraDto.infoDetailed().disponibilidade());
        }
        
        // Converter indicadores fundamentalistas
        if (infraDto.fundamentalIndicators() != null) {
            rawData.put("indicadores", infraDto.fundamentalIndicators());
        }
        
        return rawData;
    }
    
    /**
     * Determina status de processamento baseado na completude dos dados da Ação.
     * 
     * <p>Método auxiliar do MapStruct que analisa a qualidade e completude
     * dos dados coletados para determinar o status apropriado.</p>
     * 
     * @param infraDto DTO de infraestrutura da ação
     * @return Status de processamento (SUCCESS, PARTIAL, FAILED)
     */
    @Named("determineAcaoProcessingStatus")
    default ProcessingStatus determineAcaoProcessingStatus(AcaoDadosFinanceirosDTO infraDto) {
        if (infraDto == null) return ProcessingStatus.FAILED;
        
        Map<String, Object> rawData = buildAcaoRawDataMap(infraDto);
        return determineProcessingStatus(rawData);
    }
    
    /**
     * Constrói metadados descritivos sobre os dados coletados da Ação.
     * 
     * <p>Método auxiliar do MapStruct que gera informações estatísticas
     * sobre a completude e qualidade dos dados coletados.</p>
     * 
     * @param infraDto DTO de infraestrutura da ação
     * @return Mapa com metadados descritivos
     */
    @Named("buildAcaoMetadata")
    default Map<String, String> buildAcaoMetadata(AcaoDadosFinanceirosDTO infraDto) {
        if (infraDto == null) {
            return Map.of("total_fields", "0");
        }
        
        Map<String, Object> rawData = buildAcaoRawDataMap(infraDto);
        return Map.of(
            "total_fields", String.valueOf(rawData.size()),
            "has_header", String.valueOf(infraDto.infoHeader() != null),
            "has_cards", String.valueOf(infraDto.infoCards() != null),
            "has_detailed", String.valueOf(infraDto.infoDetailed() != null),
            "has_indicators", String.valueOf(infraDto.fundamentalIndicators() != null)
        );
    }
    
    /**
     * Converte FiiDadosFinanceirosDTO (infraestrutura) para FiiRawDataResponse (application).
     * Utiliza MapStruct para mapeamento declarativo com métodos auxiliares para lógica complexa.
     * 
     * @param infraDto DTO de infraestrutura com dados coletados
     * @param apiUrls URLs das APIs capturadas durante o scraping
     * @return DTO da application estruturado
     */
    default FiiRawDataResponse toFiiRawDataResponse(FiiDadosFinanceirosDTO infraDto, Map<String, String> apiUrls) {
        if (infraDto == null) {
            return createFailedFiiResponse("UNKNOWN", "SCRAPER", "DTO de infraestrutura nulo");
        }
        
        String ticker = infraDto.infoHeader() != null ? infraDto.infoHeader().ticker() : "UNKNOWN";
        Map<String, Object> rawData = buildFiiRawDataMap(infraDto);
        ProcessingStatus status = determineFiiProcessingStatus(infraDto);
        Map<String, String> metadata = buildFiiMetadata(infraDto, apiUrls);
        
        return new FiiRawDataResponse(
                ticker,
                rawData,
                "PLAYWRIGHT_SCRAPER",
                LocalDateTime.now(),
                status,
                metadata,
                apiUrls != null ? apiUrls : Map.of()
        );
    }
    
    /**
     * Constrói mapa de dados brutos para FII a partir do DTO de infraestrutura.
     * 
     * <p>Método auxiliar do MapStruct que extrai e organiza dados específicos de FII
     * de diferentes seções do DTO em um mapa chave-valor estruturado. Inclui dados
     * de header, cards, informações sobre o fundo, cotação, histórico e dividendos.</p>
     * 
     * @param infraDto DTO de infraestrutura do FII
     * @return Mapa com dados brutos organizados por categoria
     */
    @Named("buildFiiRawDataMap")
    default Map<String, Object> buildFiiRawDataMap(FiiDadosFinanceirosDTO infraDto) {
        Map<String, Object> rawData = Collections.synchronizedMap(new HashMap<>());
        
        if (infraDto == null) return rawData;
        
        // Converter dados do header
        if (infraDto.infoHeader() != null) {
            rawData.put("ticker", infraDto.infoHeader().ticker());
            rawData.put("nomeEmpresa", infraDto.infoHeader().nomeEmpresa());
        }
        
        // Converter dados dos cards
        if (infraDto.infoCards() != null) {
            rawData.put("cotacao", infraDto.infoCards().cotacao());
            rawData.put("variacao12M", infraDto.infoCards().variacao12M());
        }
        
        // Converter informações sobre o fundo
        if (infraDto.infoSobre() != null) {
            rawData.put("infoSobre", infraDto.infoSobre());
        }
        
        // Converter dados de cotação da API
        if (infraDto.cotacao() != null) {
            rawData.put("cotacaoApi", infraDto.cotacao());
        }
        
        // Converter histórico de indicadores
        if (infraDto.infoHistorico() != null) {
            rawData.put("indicadorHistorico", infraDto.infoHistorico());
        }
        
        // Converter dividendos
        if (infraDto.dividendos() != null) {
            rawData.put("dividendos", infraDto.dividendos());
        }
        
        return rawData;
    }
    
    /**
     * Determina status de processamento baseado na completude dos dados do FII.
     * 
     * <p>Método auxiliar do MapStruct que analisa a qualidade e completude
     * dos dados específicos de FII para determinar o status apropriado.</p>
     * 
     * @param infraDto DTO de infraestrutura do FII
     * @return Status de processamento (SUCCESS, PARTIAL, FAILED)
     */
    @Named("determineFiiProcessingStatus")
    default ProcessingStatus determineFiiProcessingStatus(FiiDadosFinanceirosDTO infraDto) {
        if (infraDto == null) return ProcessingStatus.FAILED;
        
        Map<String, Object> rawData = buildFiiRawDataMap(infraDto);
        return determineProcessingStatus(rawData);
    }
    
    /**
     * Constrói metadados descritivos sobre os dados coletados do FII.
     * 
     * <p>Método auxiliar do MapStruct que gera informações estatísticas específicas
     * de FII, incluindo contagem de APIs capturadas e completude de seções específicas.</p>
     * 
     * @param infraDto DTO de infraestrutura do FII
     * @param apiUrls URLs das APIs capturadas durante o scraping
     * @return Mapa com metadados descritivos específicos de FII
     */
    @Named("buildFiiMetadata")
    default Map<String, String> buildFiiMetadata(FiiDadosFinanceirosDTO infraDto, Map<String, String> apiUrls) {
        if (infraDto == null) {
            return Map.of("total_fields", "0", "apis_captured", "0");
        }
        
        Map<String, Object> rawData = buildFiiRawDataMap(infraDto);
        return Map.of(
            "total_fields", String.valueOf(rawData.size()),
            "apis_captured", String.valueOf(apiUrls != null ? apiUrls.size() : 0),
            "has_header", String.valueOf(infraDto.infoHeader() != null),
            "has_cards", String.valueOf(infraDto.infoCards() != null),
            "has_sobre", String.valueOf(infraDto.infoSobre() != null),
            "has_cotacao", String.valueOf(infraDto.cotacao() != null),
            "has_dividendos", String.valueOf(infraDto.dividendos() != null),
            "has_historico", String.valueOf(infraDto.infoHistorico() != null)
        );
    }
    
    /**
     * Determina status de processamento baseado na análise de completude dos dados.
     * 
     * <p>Método utilitário compartilhado que implementa a lógica de negócio para
     * determinar se os dados coletados estão completos, parciais ou falharam completamente.
     * Verifica a presença de ticker e dados de valor para classificação.</p>
     * 
     * @param rawData Mapa com dados brutos coletados
     * @return Status de processamento determinado pela análise
     */
    default ProcessingStatus determineProcessingStatus(Map<String, Object> rawData) {
        if (rawData.isEmpty()) {
            return ProcessingStatus.FAILED;
        }
        
        // Verificar se tem dados essenciais (ticker e pelo menos um campo de valor)
        boolean hasTicker = rawData.containsKey("ticker") && rawData.get("ticker") != null;
        boolean hasValueData = rawData.values().stream()
                .anyMatch(value -> value != null && !value.toString().trim().isEmpty());
        
        if (!hasTicker || !hasValueData) {
            return ProcessingStatus.PARTIAL;
        }
        
        // Se tem dados essenciais, considerar sucesso
        return ProcessingStatus.SUCCESS;
    }
    
    /**
     * Cria resposta de falha padronizada para operações de Ação.
     * 
     * <p>Método de conveniência que encapsula a criação de respostas de erro
     * para operações de scraping de ações que falharam completamente.</p>
     * 
     * @param ticker Código da ação
     * @param source Fonte da operação (ex: "SCRAPER")
     * @param error Mensagem de erro descritiva
     * @return Resposta de falha estruturada para ação
     */
    default AcaoRawDataResponse createFailedAcaoResponse(String ticker, String source, String error) {
        return AcaoRawDataResponse.failed(ticker, source, error);
    }
    
    /**
     * Cria resposta de falha padronizada para operações de FII.
     * 
     * <p>Método de conveniência que encapsula a criação de respostas de erro
     * para operações de scraping de FIIs que falharam completamente.</p>
     * 
     * @param ticker Código do FII
     * @param source Fonte da operação (ex: "SCRAPER")
     * @param error Mensagem de erro descritiva
     * @return Resposta de falha estruturada para FII
     */
    default FiiRawDataResponse createFailedFiiResponse(String ticker, String source, String error) {
        return FiiRawDataResponse.failed(ticker, source, error);
    }
}