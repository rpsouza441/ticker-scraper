# Implementação Detalhada - API Unificada

## 📋 Resumo Executivo

Este documento complementa o plano de migração com detalhes específicos de implementação, incluindo código de exemplo, configurações e sequência de execução.

## 🔍 Análise Detalhada do Estado Atual

### Arquitetura Hexagonal Existente

O projeto já implementa corretamente a arquitetura hexagonal:

```
DOMAIN (Núcleo)
├── model/
│   ├── Acao.java (50+ campos financeiros)
│   ├── FundoImobiliario.java (25+ campos + dividendos)
│   └── FiiDividendo.java
└── exception/ (11 exceções específicas)

APPLICATION (Casos de Uso)
├── port/input/ (Contratos de entrada)
│   ├── AcaoUseCasePort
│   └── FiiUseCasePort
├── port/output/ (Contratos de saída)
│   ├── AcaoDataScrapperPort, AcaoRepositoryPort
│   └── FiiDataScrapperPort, FiiRepositoryPort
├── service/ (Lógica de negócio)
│   ├── AbstractTickerUseCaseService (Template Method)
│   ├── AcaoUseCaseService
│   └── FiiUseCaseService
└── dto/ (Transferência de dados)

ADAPTERS (Infraestrutura)
├── input/web/ (Controllers REST)
│   ├── AcaoController, FiiController
│   ├── dto/ (AcaoResponseDTO, FiiResponseDTO)
│   └── mapper/ (MapStruct)
└── output/persistence/ (JPA)
    ├── entity/ (AcaoEntity, FundoImobiliarioEntity)
    ├── jpa/ (Repositories)
    └── mapper/ (Entity ↔ Domain)
```

### Pontos Fortes Identificados

1. **Separação clara de responsabilidades**
2. **Template Method bem implementado** (`AbstractTickerUseCaseService`)
3. **Programação reativa** com WebFlux
4. **Cache inteligente** (24h de validade)
5. **Auditoria completa** (`dados_brutos_json`)
6. **Resiliência** com Resilience4j

### Gaps Identificados

1. **Falta coluna `tipo_codigo`** nas entidades
2. **APIs separadas** para ação e FII
3. **Sem classificação automática** de ativos
4. **Sem integração externa** para validação

## 🚀 Implementação Passo a Passo

### PASSO 1: Migração do Banco de Dados

#### 1.1 Criar Migração Flyway

**Arquivo**: `src/main/resources/db/migration/V1__add_tipo_codigo_columns.sql`

```sql
-- =====================================================
-- Migração V1: Adicionar coluna tipo_codigo
-- Data: Janeiro 2025
-- Descrição: Suporte para classificação de ativos
-- =====================================================

-- Adicionar coluna tipo_codigo nas tabelas existentes
ALTER TABLE acao 
ADD COLUMN tipo_codigo VARCHAR(20) DEFAULT 'DESCONHECIDO';

ALTER TABLE fundo_imobiliario 
ADD COLUMN tipo_codigo VARCHAR(20) DEFAULT 'DESCONHECIDO';

-- Atualizar registros existentes com valores padrão
UPDATE acao SET tipo_codigo = 'ACAO' WHERE tipo_codigo = 'DESCONHECIDO';
UPDATE fundo_imobiliario SET tipo_codigo = 'FII' WHERE tipo_codigo = 'DESCONHECIDO';

-- Criar índices para performance
CREATE INDEX idx_acao_tipo_codigo ON acao(tipo_codigo);
CREATE INDEX idx_acao_ticker_tipo ON acao(ticker, tipo_codigo);
CREATE INDEX idx_fii_tipo_codigo ON fundo_imobiliario(tipo_codigo);
CREATE INDEX idx_fii_ticker_tipo ON fundo_imobiliario(ticker, tipo_codigo);

-- Adicionar comentários para documentação
COMMENT ON COLUMN acao.tipo_codigo IS 'Tipo do ativo: ACAO, UNIT, ETF, DESCONHECIDO';
COMMENT ON COLUMN fundo_imobiliario.tipo_codigo IS 'Tipo do ativo: FII, DESCONHECIDO';

-- Criar constraint para valores válidos (opcional)
ALTER TABLE acao 
ADD CONSTRAINT chk_acao_tipo_codigo 
CHECK (tipo_codigo IN ('ACAO', 'UNIT', 'ETF', 'DESCONHECIDO'));

ALTER TABLE fundo_imobiliario 
ADD CONSTRAINT chk_fii_tipo_codigo 
CHECK (tipo_codigo IN ('FII', 'DESCONHECIDO'));
```

#### 1.2 Atualizar Entidades JPA

**Arquivo**: `adapter/output/persistence/entity/AcaoEntity.java`

```java
// Adicionar após os campos existentes
@Enumerated(EnumType.STRING)
@Column(name = "tipo_codigo", length = 20)
@Builder.Default
private TipoAtivo tipoCodigo = TipoAtivo.DESCONHECIDO;

// Getter e Setter
public TipoAtivo getTipoCodigo() {
    return tipoCodigo;
}

public void setTipoCodigo(TipoAtivo tipoCodigo) {
    this.tipoCodigo = tipoCodigo;
}
```

**Arquivo**: `adapter/output/persistence/entity/FundoImobiliarioEntity.java`

```java
// Adicionar após os campos existentes
@Enumerated(EnumType.STRING)
@Column(name = "tipo_codigo", length = 20)
@Builder.Default
private TipoAtivo tipoCodigo = TipoAtivo.FII;

// Getter e Setter
public TipoAtivo getTipoCodigo() {
    return tipoCodigo;
}

public void setTipoCodigo(TipoAtivo tipoCodigo) {
    this.tipoCodigo = tipoCodigo;
}
```

### PASSO 2: Modelos de Domínio

#### 2.1 Enum TipoAtivo

**Arquivo**: `domain/model/TipoAtivo.java`

```java
package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration dos tipos de ativos financeiros suportados.
 * 
 * @author Rodrigo Pinheiro
 * @since 1.0
 */
public enum TipoAtivo {
    
    ACAO("ACAO", "Ação", "Ação ordinária ou preferencial"),
    UNIT("UNIT", "Unit", "Certificado de depósito de ações"),
    FII("FII", "Fundo de Investimento Imobiliário", "Fundo de investimento imobiliário"),
    ETF("ETF", "Exchange Traded Fund", "Fundo de índice negociado em bolsa"),
    DESCONHECIDO("DESCONHECIDO", "Tipo Desconhecido", "Tipo não identificado ou classificado");
    
    private final String codigo;
    private final String nome;
    private final String descricao;
    
    TipoAtivo(String codigo, String nome, String descricao) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
    }
    
    @JsonValue
    public String getCodigo() {
        return codigo;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    @JsonCreator
    public static TipoAtivo fromCodigo(String codigo) {
        if (codigo == null) {
            return DESCONHECIDO;
        }
        
        for (TipoAtivo tipo : values()) {
            if (tipo.codigo.equalsIgnoreCase(codigo)) {
                return tipo;
            }
        }
        
        return DESCONHECIDO;
    }
    
    /**
     * Verifica se o tipo é um ativo de renda variável tradicional.
     */
    public boolean isRendaVariavel() {
        return this == ACAO || this == UNIT;
    }
    
    /**
     * Verifica se o tipo é um fundo.
     */
    public boolean isFundo() {
        return this == FII || this == ETF;
    }
    
    @Override
    public String toString() {
        return codigo;
    }
}
```

#### 2.2 Enum FonteClassificacao

**Arquivo**: `domain/model/FonteClassificacao.java`

```java
package br.dev.rodrigopinheiro.tickerscraper.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration das fontes de classificação de ativos.
 * 
 * @author Rodrigo Pinheiro
 * @since 1.0
 */
public enum FonteClassificacao {
    
    BRAPI("brapi", "API Brapi", "Classificação obtida via API Brapi", true),
    HEURISTICA("heuristica", "Heurística", "Classificação por regras heurísticas", false),
    MANUAL("manual", "Manual", "Classificação manual pelo usuário", true),
    CACHE("cache", "Cache", "Classificação obtida do cache local", false);
    
    private final String codigo;
    private final String nome;
    private final String descricao;
    private final boolean confiavel;
    
    FonteClassificacao(String codigo, String nome, String descricao, boolean confiavel) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.confiavel = confiavel;
    }
    
    @JsonValue
    public String getCodigo() {
        return codigo;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Indica se a fonte é considerada confiável para persistência.
     */
    public boolean isConfiavel() {
        return confiavel;
    }
    
    @JsonCreator
    public static FonteClassificacao fromCodigo(String codigo) {
        if (codigo == null) {
            return HEURISTICA;
        }
        
        for (FonteClassificacao fonte : values()) {
            if (fonte.codigo.equalsIgnoreCase(codigo)) {
                return fonte;
            }
        }
        
        return HEURISTICA;
    }
    
    @Override
    public String toString() {
        return codigo;
    }
}
```

### PASSO 3: Integração com API Brapi

#### 3.1 DTOs da API Brapi

**Arquivo**: `infrastructure/http/brapi/dto/BrapiQuoteResponse.java`

```java
package br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response da API Brapi para consulta de cotações.
 * 
 * @see <a href="https://brapi.dev/docs/quote">Documentação Brapi</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BrapiQuoteResponse(
    @JsonProperty("results")
    List<BrapiQuoteResult> results,
    
    @JsonProperty("requestedAt")
    String requestedAt,
    
    @JsonProperty("took")
    String took
) {
    
    /**
     * Verifica se a resposta contém resultados válidos.
     */
    public boolean hasResults() {
        return results != null && !results.isEmpty();
    }
    
    /**
     * Obtém o primeiro resultado, se disponível.
     */
    public BrapiQuoteResult getFirstResult() {
        return hasResults() ? results.get(0) : null;
    }
}

/**
 * Resultado individual de uma consulta de cotação.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BrapiQuoteResult(
    @JsonProperty("symbol")
    String symbol,
    
    @JsonProperty("shortName")
    String shortName,
    
    @JsonProperty("longName")
    String longName,
    
    @JsonProperty("currency")
    String currency,
    
    @JsonProperty("regularMarketPrice")
    Double regularMarketPrice,
    
    @JsonProperty("marketCap")
    String marketCap,
    
    @JsonProperty("logourl")
    String logoUrl
) {
    
    /**
     * Obtém o nome completo para análise (shortName + longName).
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        
        if (shortName != null && !shortName.trim().isEmpty()) {
            fullName.append(shortName.trim());
        }
        
        if (longName != null && !longName.trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(longName.trim());
        }
        
        return fullName.toString().toUpperCase();
    }
    
    /**
     * Verifica se o resultado tem informações mínimas necessárias.
     */
    public boolean isValid() {
        return symbol != null && !symbol.trim().isEmpty() &&
               (shortName != null || longName != null);
    }
}
```

#### 3.2 Cliente HTTP Brapi

**Arquivo**: `infrastructure/http/brapi/BrapiHttpClient.java`

```java
package br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi;

import br.dev.rodrigopinheiro.tickerscraper.domain.exception.NetworkCaptureException;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cliente HTTP para integração com a API Brapi.
 * 
 * Implementa retry com backoff exponencial e jitter para resiliência.
 * 
 * @author Rodrigo Pinheiro
 * @since 1.0
 */
@Component
public class BrapiHttpClient {
    
    private static final Logger log = LoggerFactory.getLogger(BrapiHttpClient.class);
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String token;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    
    public BrapiHttpClient(
            @Value("${brapi.base-url:https://brapi.dev/api}") String baseUrl,
            @Value("${brapi.token:}") String token,
            @Value("${brapi.connect-timeout:2s}") Duration connectTimeout,
            @Value("${brapi.read-timeout:2s}") Duration readTimeout,
            ObjectMapper objectMapper) {
        
        this.baseUrl = baseUrl;
        this.token = token;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.objectMapper = objectMapper;
        
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .build();
        
        log.info("BrapiHttpClient inicializado - baseUrl: {}, timeout: {}s", 
                baseUrl, connectTimeout.getSeconds());
    }
    
    /**
     * Consulta informações de um ticker na API Brapi.
     * 
     * @param ticker Código do ativo (ex: PETR4, HGLG11)
     * @return Mono com a resposta da API
     */
    public Mono<BrapiQuoteResponse> getQuote(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Ticker não pode ser nulo ou vazio"));
        }
        
        String normalizedTicker = ticker.trim().toUpperCase();
        log.debug("Consultando ticker {} na API Brapi", normalizedTicker);
        
        return Mono.fromCallable(() -> executeRequest(normalizedTicker))
            .retryWhen(createRetrySpec())
            .doOnSuccess(response -> log.debug("Resposta recebida para ticker {}: {} resultados", 
                    normalizedTicker, response.results().size()))
            .doOnError(error -> log.error("Erro ao consultar ticker {} na API Brapi", 
                    normalizedTicker, error));
    }
    
    private BrapiQuoteResponse executeRequest(String ticker) throws Exception {
        String url = buildUrl(ticker);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(readTimeout)
            .header("Accept", "application/json")
            .header("User-Agent", "TickerScraper/1.0")
            .GET()
            .build();
        
        log.trace("Executando request: {}", url.replaceAll("token=[^&]*", "token=***"));
        
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new NetworkCaptureException(
                String.format("API Brapi retornou status %d para ticker %s", 
                    response.statusCode(), ticker));
        }
        
        return objectMapper.readValue(response.body(), BrapiQuoteResponse.class);
    }
    
    private String buildUrl(String ticker) {
        StringBuilder url = new StringBuilder(baseUrl)
            .append("/quote/")
            .append(ticker);
        
        if (token != null && !token.trim().isEmpty()) {
            url.append("?token=").append(token);
        }
        
        return url.toString();
    }
    
    private Retry createRetrySpec() {
        return Retry.backoff(3, Duration.ofMillis(200))
            .maxBackoff(Duration.ofMillis(800))
            .jitter(0.1) // 10% de jitter
            .filter(this::isRetryableException)
            .doBeforeRetry(retrySignal -> {
                log.warn("Tentativa {} de {} para API Brapi - Erro: {}", 
                    retrySignal.totalRetries() + 1, 3, 
                    retrySignal.failure().getMessage());
            });
    }
    
    private boolean isRetryableException(Throwable throwable) {
        // Retry em casos de timeout, conexão ou erros 5xx
        return throwable instanceof java.net.SocketTimeoutException ||
               throwable instanceof java.net.ConnectException ||
               throwable instanceof java.io.IOException ||
               (throwable instanceof NetworkCaptureException && 
                throwable.getMessage().contains("50"));
    }
    
    /**
     * Adiciona jitter aleatório para evitar thundering herd.
     */
    private Duration addJitter(Duration duration) {
        long baseMillis = duration.toMillis();
        long jitterMillis = ThreadLocalRandom.current().nextLong(
            (long) (baseMillis * 0.1)); // 10% de jitter
        return Duration.ofMillis(baseMillis + jitterMillis);
    }
}
```

### PASSO 4: Serviço de Classificação

#### 4.1 Port de Classificação

**Arquivo**: `application/port/output/ClassificadorAtivoPort.java`

```java
package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.FonteClassificacao;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.TipoAtivo;
import reactor.core.publisher.Mono;

/**
 * Port para classificação de ativos financeiros.
 * 
 * @author Rodrigo Pinheiro
 * @since 1.0
 */
public interface ClassificadorAtivoPort {
    
    /**
     * Classifica um ativo pelo seu código.
     * 
     * @param codigo Código do ativo (ex: PETR4, HGLG11)
     * @return Mono com o resultado da classificação
     */
    Mono<ClassificacaoResult> classificar(String codigo);
}

/**
 * Resultado da classificação de um ativo.
 * 
 * @param codigo Código normalizado do ativo
 * @param categoria Tipo identificado do ativo
 * @param fonte Fonte da classificação
 * @param detalhes Detalhes sobre como a classificação foi feita
 * @param confianca Nível de confiança (0.0 a 1.0)
 */
public record ClassificacaoResult(
    String codigo,
    TipoAtivo categoria,
    FonteClassificacao fonte,
    String detalhes,
    double confianca
) {
    
    /**
     * Cria um resultado com confiança máxima.
     */
    public static ClassificacaoResult confiavel(String codigo, TipoAtivo categoria, 
                                               FonteClassificacao fonte, String detalhes) {
        return new ClassificacaoResult(codigo, categoria, fonte, detalhes, 1.0);
    }
    
    /**
     * Cria um resultado com confiança baixa (heurística).
     */
    public static ClassificacaoResult heuristico(String codigo, TipoAtivo categoria, String detalhes) {
        return new ClassificacaoResult(codigo, categoria, FonteClassificacao.HEURISTICA, detalhes, 0.6);
    }
    
    /**
     * Verifica se o resultado é confiável o suficiente para persistir.
     */
    public boolean isConfiavel() {
        return fonte.isConfiavel() && confianca >= 0.8;
    }
}
```

## 📝 Próximos Passos de Implementação

### Sequência Recomendada

1. **Executar migração do banco** (V1__add_tipo_codigo_columns.sql)
2. **Implementar enums** (TipoAtivo, FonteClassificacao)
3. **Atualizar entidades JPA** com nova coluna
4. **Implementar cliente Brapi** com testes unitários
5. **Criar serviço de classificação** com regras de negócio
6. **Implementar API unificada** (/ativo/{codigo})
7. **Implementar endpoint de classificação** (/classifica/{codigo})
8. **Testes de integração** completos
9. **Documentação da API** (OpenAPI/Swagger)
10. **Deploy e monitoramento**

### Configurações Necessárias

**application.yml**
```yaml
# Configurações da API Brapi
brapi:
  base-url: https://brapi.dev/api
  token: ${BRAPI_TOKEN:}
  connect-timeout: 2s
  read-timeout: 2s
  retry:
    max-attempts: 3
    initial-delay: 200ms
    multiplier: 2.0
    max-delay: 800ms
    jitter: 0.1

# Cache para classificações
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=1h
```

### Testes Essenciais

1. **Testes de Migração**: Verificar schema do banco
2. **Testes do Cliente Brapi**: Mock da API externa
3. **Testes de Classificação**: Cenários de regras de negócio
4. **Testes de Integração**: Fluxo completo end-to-end
5. **Testes de Performance**: Carga e stress

---

**Próximo Documento**: `docs/testes-e-validacao.md`