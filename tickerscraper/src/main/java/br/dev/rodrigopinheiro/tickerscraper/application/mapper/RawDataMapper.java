package br.dev.rodrigopinheiro.tickerscraper.application.mapper;

import br.dev.rodrigopinheiro.tickerscraper.application.dto.*;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.dto.EtfDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDadosFinanceirosDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.*;

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
 * @see Mapper
 * @see AcaoRawDataResponse
 * @see EtfRawDataResponse
 * @see FiiRawDataResponse
 */
@Mapper(componentModel = "spring")
public interface RawDataMapper {

    /**
     * Converte DTO de infraestrutura de Ação para DTO da camada application.
     *
     * <p>Utiliza mapeamento declarativo do MapStruct com Methods auxiliares qualificados
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
     * <p>Method auxiliar do MapStruct que extrai e organiza dados de diferentes
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
     * <p>method auxiliar do MapStruct que analisa a qualidade e completude
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
     * <p>Method auxiliar do MapStruct que gera informações estatísticas
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
     * Utiliza MapStruct para mapeamento declarativo com Methods auxiliares para lógica complexa.
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
     * <p>Method auxiliar do MapStruct que extrai e organiza dados específicos de FII
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
     * <p>Method auxiliar do MapStruct que analisa a qualidade e completude
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
     * <p>Method auxiliar do MapStruct que gera informações estatísticas específicas
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
     * <p>Method utilitário compartilhado que implementa a lógica de negócio para
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
     * <p>Method de conveniência que encapsula a criação de respostas de erro
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
     * <p>Method de conveniência que encapsula a criação de respostas de erro
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

    /**
     * Converte EtfDadosFinanceirosDTO (infraestrutura) para EtfRawDataResponse (application).
     * Utiliza MapStruct para mapeamento declarativo com Methods auxiliares para lógica complexa.
     *
     * @param infraDto DTO de infraestrutura com dados coletados
     * @return DTO da application estruturado
     */
    default EtfRawDataResponse toEtfRawDataResponse(EtfDadosFinanceirosDTO infraDto) {
        if (infraDto == null) {
            return createFailedEtfResponse("UNKNOWN", "SCRAPER", "DTO de infraestrutura nulo");
        }

        String ticker = infraDto.infoHeader() != null ? infraDto.infoHeader().ticker() : "UNKNOWN";
        Map<String, Object> rawData = buildEtfRawDataMap(infraDto);
        ProcessingStatus status = determineEtfProcessingStatus(infraDto);
        Map<String, String> metadata = buildEtfMetadata(infraDto);

        return new EtfRawDataResponse(
                ticker,
                rawData,
                "PLAYWRIGHT_SCRAPER",
                LocalDateTime.now(),
                status,
                metadata
        );
    }

    /**
     * Constrói mapa de dados brutos para ETF a partir do DTO de infraestrutura.
     *
     * <p>Method auxiliar do MapStruct que extrai e organiza dados específicos de ETF
     * de diferentes seções do DTO em um mapa chave-valor estruturado. Inclui dados
     * de header e cards com informações financeiras do ETF.</p>
     *
     * @param infraDto DTO de infraestrutura do ETF
     * @return Mapa com dados brutos organizados por categoria
     */
    @Named("buildEtfRawDataMap")
    default Map<String, Object> buildEtfRawDataMap(EtfDadosFinanceirosDTO infraDto) {
        Map<String, Object> rawData = Collections.synchronizedMap(new HashMap<>());

        if (infraDto == null) return rawData;

        // Converter dados do header
        if (infraDto.infoHeader() != null) {
            rawData.put("ticker", infraDto.infoHeader().ticker());
            rawData.put("nomeEtf", infraDto.infoHeader().nomeEtf());
        }

        // Converter dados dos cards
        if (infraDto.infoCards() != null) {
            rawData.put("valorAtual", infraDto.infoCards().valorAtual());
            rawData.put("capitalizacao", infraDto.infoCards().capitalizacao());
            rawData.put("variacao12M", infraDto.infoCards().variacao12M());
            rawData.put("variacao60M", infraDto.infoCards().variacao60M());
            rawData.put("dy", infraDto.infoCards().dy());
        }

        return rawData;
    }

    /**
     * Determina status de processamento baseado na completude dos dados do ETF.
     *
     * <p>Method auxiliar do MapStruct que analisa a qualidade e completude
     * dos dados específicos de ETF para determinar o status apropriado.</p>
     *
     * @param infraDto DTO de infraestrutura do ETF
     * @return Status de processamento (SUCCESS, PARTIAL, FAILED)
     */
    @Named("determineEtfProcessingStatus")
    default ProcessingStatus determineEtfProcessingStatus(EtfDadosFinanceirosDTO infraDto) {
        if (infraDto == null) return ProcessingStatus.FAILED;

        Map<String, Object> rawData = buildEtfRawDataMap(infraDto);
        return determineProcessingStatus(rawData);
    }

    /**
     * Constrói metadados descritivos sobre os dados coletados do ETF.
     *
     * <p>Method auxiliar do MapStruct que gera informações estatísticas específicas
     * de ETF, incluindo completude de seções específicas.</p>
     *
     * @param infraDto DTO de infraestrutura do ETF
     * @return Mapa com metadados descritivos específicos de ETF
     */
    @Named("buildEtfMetadata")
    default Map<String, String> buildEtfMetadata(EtfDadosFinanceirosDTO infraDto) {
        if (infraDto == null) {
            return Map.of("total_fields", "0");
        }

        Map<String, Object> rawData = buildEtfRawDataMap(infraDto);
        return Map.of(
                "total_fields", String.valueOf(rawData.size()),
                "has_header", String.valueOf(infraDto.infoHeader() != null),
                "has_cards", String.valueOf(infraDto.infoCards() != null)
        );
    }

    /**
     * Cria resposta de falha padronizada para operações de ETF.
     *
     * <p>Method de conveniência que encapsula a criação de respostas de erro
     * para operações de scraping de ETFs que falharam completamente.</p>
     *
     * @param ticker Código do ETF
     * @param source Fonte da operação (ex: "SCRAPER")
     * @param error Mensagem de erro descritiva
     * @return Resposta de falha estruturada para ETF
     */
    default EtfRawDataResponse createFailedEtfResponse(String ticker, String source, String error) {
        return EtfRawDataResponse.failed(ticker, source, error);
    }



    // Adicionar ao RawDataMapper existente:

    /**
     * Converte BdrDadosFinanceirosDTO para BdrRawDataResponse.
     *
     * @param infraDto DTO de infraestrutura com dados coletados
     * @param apiUrls URLs das APIs capturadas (pode ser null)
     * @return DTO da application estruturado
     */
    default BdrRawDataResponse toBdrRawDataResponse(
            BdrDadosFinanceirosDTO infraDto,
            Map<String, String> apiUrls
    ) {
        if (infraDto == null) {
            return BdrRawDataResponse.failed("UNKNOWN", "SCRAPER", "DTO de infraestrutura nulo");
        }

        String ticker = infraDto.infoHeader() != null ?
                infraDto.infoHeader().ticker() : "UNKNOWN";

        try {
            // Build raw data map
            Map<String, Object> rawData = buildBdrRawDataMap(infraDto);

            // Build metadata
            Map<String, Object> metadata = buildBdrMetadata(infraDto, apiUrls);

            // Determine processing status
            ProcessingStatus status = determineBdrProcessingStatus(infraDto);

            // Return appropriate response based on status
            return switch (status) {
                case SUCCESS -> BdrRawDataResponse.success(ticker, rawData, "SCRAPER", metadata);
                case PARTIAL -> {
                    String reason = determinePartialReason(infraDto);
                    yield BdrRawDataResponse.partial(ticker, rawData, "SCRAPER", metadata, reason);
                }
                case FAILED -> BdrRawDataResponse.failed(ticker, "SCRAPER", "Dados insuficientes");
                case CACHED -> null;
                case FALLBACK -> null;
            };

        } catch (Exception e) {
            return BdrRawDataResponse.failed(ticker, "SCRAPER", "Erro no mapeamento: " + e.getMessage());
        }
    }


    /**
     * Determina a razão específica para status PARTIAL
     */
    @Named("determinePartialReason")
    default String determinePartialReason(BdrDadosFinanceirosDTO infraDto) {
        List<String> missing = new ArrayList<>();

        if (infraDto.infoHeader() == null || infraDto.infoHeader().ticker() == null) {
            missing.add("header");
        }
        if (infraDto.infoCards() == null || infraDto.infoCards().cotacao() == null) {
            missing.add("cotacao");
        }
        if (infraDto.indicadores() == null || infraDto.indicadores().isEmpty()) {
            missing.add("indicadores");
        }
        if (infraDto.demonstrativos() == null ||
                ((infraDto.demonstrativos().dre() == null || infraDto.demonstrativos().dre().isEmpty()) &&
                        (infraDto.demonstrativos().bp() == null || infraDto.demonstrativos().bp().isEmpty()) &&
                        (infraDto.demonstrativos().fc() == null || infraDto.demonstrativos().fc().isEmpty()))) {
            missing.add("demonstrativos");
        }


        return missing.isEmpty() ? "Dados incompletos" :
                "Faltando: " + String.join(", ", missing);
    }
    /**
     * Method auxiliar do MapStruct que extrai e organiza dados específicos de BDR
     * de diferentes seções do DTO em um mapa chave-valor estruturado. Inclui dados
     * de header, cards, informações sobre o BDR, indicadores, demonstrativos e dividendos.
     *
     * @param infraDto DTO de infraestrutura do BDR
     * @return Mapa com dados brutos organizados por categoria
     */
    @Named("buildBdrRawDataMap")
    default Map<String, Object> buildBdrRawDataMap(BdrDadosFinanceirosDTO infraDto) {
        if (infraDto == null) return Collections.emptyMap();

        Map<String, Object> rawData = new HashMap<>();

        // Header
        if (infraDto.infoHeader() != null) {
            Map<String, Object> header = new HashMap<>();
            header.put("ticker", infraDto.infoHeader().ticker());
            header.put("nomeBdr", infraDto.infoHeader().nomeBdr());
            rawData.put("header", header);
        }

        // Cards (cotação, variação)
        if (infraDto.infoCards() != null) {
            Map<String, Object> cards = new HashMap<>();
            cards.put("cotacao", infraDto.infoCards().cotacao());
            cards.put("variacao12M", infraDto.infoCards().variacao12M());
            rawData.put("cards", cards);
        }

        // Info Sobre (setor, indústria, market cap, paridade)
        if (infraDto.infoSobre() != null) {
            Map<String, Object> sobre = new HashMap<>();
            sobre.put("marketCapText", infraDto.infoSobre().marketCapText());
            sobre.put("setor", infraDto.infoSobre().setor());
            sobre.put("industria", infraDto.infoSobre().industria());
            sobre.put("paridadeText", infraDto.infoSobre().paridadeText());
            rawData.put("sobre", sobre);
        }

        // Indicadores
        if (infraDto.indicadores() != null && !infraDto.indicadores().isEmpty()) {
            rawData.put("indicadores", infraDto.indicadores());
        }

        // Demonstrativos
        if (infraDto.demonstrativos() != null) {
            Map<String, Object> demonstrativos = new HashMap<>();
            if (infraDto.demonstrativos().dre() != null) {
                demonstrativos.put("dre", infraDto.demonstrativos().dre());
            }
            if (infraDto.demonstrativos().bp() != null) {
                demonstrativos.put("bp", infraDto.demonstrativos().bp());
            }
            if (infraDto.demonstrativos().fc() != null) {
                demonstrativos.put("fc", infraDto.demonstrativos().fc());
            }
            if (!demonstrativos.isEmpty()) {
                rawData.put("demonstrativos", demonstrativos);
            }
        }

        // Dividendos
        if (infraDto.dividendos() != null && !infraDto.dividendos().isEmpty()) {
            rawData.put("dividendos", infraDto.dividendos());
        }

        // Timestamp
        rawData.put("updatedAt", infraDto.updatedAt());

        return rawData;
    }

    /**
     * Method auxiliar do MapStruct que analisa a qualidade e completude
     * dos dados específicos de BDR para determinar o status apropriado.
     *
     * @param infraDto DTO de infraestrutura do BDR
     * @return Status de processamento (SUCCESS, PARTIAL, FAILED)
     */
// Coloque este Method dentro de RawDataMapper.java
    @Named("determineBdrProcessingStatus")
    default ProcessingStatus determineBdrProcessingStatus(BdrDadosFinanceirosDTO infraDto) {
        if (infraDto == null || infraDto.infoHeader() == null) {
            return ProcessingStatus.FAILED;
        }

        // Define o que consideramos essencial para ser "SUCCESS"
        boolean hasHeader = infraDto.infoHeader() != null && infraDto.infoHeader().ticker() != null;
        boolean hasCards = infraDto.infoCards() != null && infraDto.infoCards().cotacao() != null;
        boolean hasSobre = infraDto.infoSobre() != null && infraDto.infoSobre().setor() != null;
        boolean hasIndicadores = infraDto.indicadores() != null && !infraDto.indicadores().isEmpty();
        boolean hasDemonstrativos = infraDto.demonstrativos() != null && infraDto.demonstrativos().dre() != null;
        boolean hasDividendos = infraDto.dividendos() != null && !infraDto.dividendos().isEmpty();

        // Se todas as seções principais foram capturadas, é sucesso.
        if (hasHeader && hasCards && hasSobre && hasIndicadores && hasDemonstrativos && hasDividendos) {
            return ProcessingStatus.SUCCESS;
        }

        // Se tivermos pelo menos o cabeçalho e mais alguma coisa, é parcial.
        if (hasHeader && (hasCards || hasSobre || hasIndicadores)) {
            return ProcessingStatus.PARTIAL;
        }

        return ProcessingStatus.FAILED;
    }

    /**
     * Method auxiliar do MapStruct que gera informações estatísticas específicas
     * de BDR, incluindo contagem de APIs capturadas e completude de seções específicas.
     *
     * @param infraDto DTO de infraestrutura do BDR
     * @param apiUrls URLs das APIs capturadas durante o scraping
     * @return Mapa com metadados descritivos específicos de BDR
     */
    @Named("buildBdrMetadata")
    default Map<String, Object> buildBdrMetadata(BdrDadosFinanceirosDTO infraDto,
                                                 Map<String, String> apiUrls) {
        if (infraDto == null) return Collections.emptyMap();

        Map<String, Object> metadata = new HashMap<>();

        // Informações básicas
        metadata.put("scrapingTimestamp", LocalDateTime.now());
        metadata.put("sourceType", "BDR");

        // Completude de seções
        int sectionsCount = 0;
        int completeSections = 0;

        if (infraDto.infoHeader() != null) {
            sectionsCount++;
            if (infraDto.infoHeader().ticker() != null) completeSections++;
        }

        if (infraDto.infoCards() != null) {
            sectionsCount++;
            if (infraDto.infoCards().cotacao() != null) completeSections++;
        }

        if (infraDto.infoSobre() != null) {
            sectionsCount++;
            if (infraDto.infoSobre().setor() != null ||
                    infraDto.infoSobre().industria() != null) {
                completeSections++;
            }
        }

        if (infraDto.indicadores() != null && !infraDto.indicadores().isEmpty()) {
            sectionsCount++;
            completeSections++;
        }

        if (infraDto.demonstrativos() != null) {
            sectionsCount++;
            if ((infraDto.demonstrativos().dre() != null && !infraDto.demonstrativos().dre().isEmpty()) ||
                    (infraDto.demonstrativos().bp() != null && !infraDto.demonstrativos().bp().isEmpty()) ||
                    (infraDto.demonstrativos().fc() != null && !infraDto.demonstrativos().fc().isEmpty())) {
                completeSections++;
            }
        }

        if (infraDto.dividendos() != null && !infraDto.dividendos().isEmpty()) {
            sectionsCount++;
            completeSections++;
        }

        metadata.put("sectionsCount", sectionsCount);
        metadata.put("completeSections", completeSections);
        metadata.put("completenessPercentage",
                sectionsCount > 0 ? (completeSections * 100.0 / sectionsCount) : 0.0);

        // APIs capturadas
        if (apiUrls != null && !apiUrls.isEmpty()) {
            metadata.put("apiUrlsCount", apiUrls.size());
            metadata.put("capturedApis", apiUrls.keySet());
        }

        // Indicadores específicos de BDR
        if (infraDto.infoSobre() != null) {
            metadata.put("hasMarketCap", infraDto.infoSobre().marketCapText() != null);
            metadata.put("hasParidade", infraDto.infoSobre().paridadeText() != null);
            metadata.put("hasSetor", infraDto.infoSobre().setor() != null);
            metadata.put("hasIndustria", infraDto.infoSobre().industria() != null);
        }

        return metadata;
    }

    /**
     * Method de conveniência que encapsula a criação de respostas de erro
     * para operações de scraping de BDRs que falharam completamente.
     *
     * @param ticker Código do BDR
     * @param source Fonte da operação (ex: "SCRAPER")
     * @param error Mensagem de erro descritiva
     * @return Resposta de falha estruturada para BDR
     */
    default BdrRawDataResponse createFailedBdrResponse(String ticker, String source, String error) {
        return BdrRawDataResponse.failed(ticker, source, error);
    }

}