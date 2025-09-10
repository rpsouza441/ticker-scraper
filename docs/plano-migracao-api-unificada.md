# Plano de Migração - API Unificada com Integração Brapi

## 📋 Visão Geral

Este documento detalha o plano de migração para implementar uma API unificada que integra com a API Brapi para classificação automática de ativos financeiros, seguindo os princípios da Arquitetura Hexagonal.

## 🎯 Objetivos

1. **Endpoint Unificado**: `GET /ativo/{codigo}` - consulta unificada para ações e FIIs
2. **Endpoint de Classificação**: `GET /classifica/{codigo}` - classificação de ativos sem persistência
3. **Integração Brapi**: Cliente HTTP para classificação automática de ativos
4. **Migração de Banco**: Nova coluna `tipo_codigo` nas tabelas existentes
5. **Arquitetura Hexagonal**: Separação clara entre domínio, aplicação e infraestrutura

## 🏗️ Análise da Arquitetura Atual

### Estrutura Existente

```
├── domain/
│   ├── model/
│   │   ├── Acao.java
│   │   ├── FundoImobiliario.java
│   │   └── FiiDividendo.java
│   └── exception/ (11 exceções específicas)
├── application/
│   ├── port/
│   │   ├── input/ (AcaoUseCasePort, FiiUseCasePort)
│   │   └── output/ (Repository e Scraper Ports)
│   ├── service/ (AcaoUseCaseService, FiiUseCaseService)
│   └── dto/ (DTOs de aplicação)
├── adapter/
│   ├── input/web/ (Controllers REST)
│   └── output/persistence/ (Entidades JPA)
└── infrastructure/
    ├── config/ (Configurações)
    ├── scraper/ (Implementações de scraping)
    └── parser/ (Parsers de dados)
```

### Componentes Identificados

#### Ports Existentes
- **AcaoDataScrapperPort**: `Mono<AcaoDadosFinanceirosDTO> scrape(String ticker)`
- **FiiDataScrapperPort**: `Mono<FiiDadosFinanceirosDTO> scrape(String ticker)`
- **AcaoRepositoryPort**: CRUD para ações
- **FiiRepositoryPort**: CRUD para FIIs

#### Serviços Existentes
- **AbstractTickerUseCaseService**: Lógica comum de cache e scraping
- **AcaoUseCaseService**: Implementação específica para ações
- **FiiUseCaseService**: Implementação específica para FIIs

#### Estrutura do Banco
- **Tabela `acao`**: 50+ campos financeiros, sem `tipo_codigo`
- **Tabela `fundo_imobiliario`**: 25+ campos, sem `tipo_codigo`
- **Tabela `fii_dividendo`**: Relacionamento com FIIs
- **Campos de auditoria**: `dados_brutos_json`, `data_atualizacao`

## 🚀 Plano de Implementação

### Fase 1: Preparação da Base

#### 1.1 Migração do Banco de Dados

**Arquivo**: `src/main/resources/db/migration/V1__add_tipo_codigo_columns.sql`

```sql
-- Adicionar coluna tipo_codigo nas tabelas existentes
ALTER TABLE acao ADD COLUMN tipo_codigo VARCHAR(20);
ALTER TABLE fundo_imobiliario ADD COLUMN tipo_codigo VARCHAR(20);

-- Criar índices para performance
CREATE INDEX idx_acao_tipo_codigo ON acao(tipo_codigo);
CREATE INDEX idx_fii_tipo_codigo ON fundo_imobiliario(tipo_codigo);

-- Comentários para documentação
COMMENT ON COLUMN acao.tipo_codigo IS 'Tipo do ativo: ACAO, UNIT, ETF, DESCONHECIDO';
COMMENT ON COLUMN fundo_imobiliario.tipo_codigo IS 'Tipo do ativo: FII, DESCONHECIDO';
```

#### 1.2 Enum de Tipos de Ativo

**Arquivo**: `domain/model/TipoAtivo.java`

```java
public enum TipoAtivo {
    ACAO("Ação"),
    UNIT("Unit"),
    FII("Fundo de Investimento Imobiliário"),
    ETF("Exchange Traded Fund"),
    DESCONHECIDO("Tipo não identificado");
    
    private final String descricao;
    
    TipoAtivo(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
```

#### 1.3 Fonte de Classificação

**Arquivo**: `domain/model/FonteClassificacao.java`

```java
public enum FonteClassificacao {
    BRAPI("Classificação via API Brapi"),
    HEURISTICA("Classificação por heurística");
    
    private final String descricao;
    
    FonteClassificacao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
```

### Fase 2: Integração com Brapi

#### 2.1 DTOs da API Brapi

**Arquivo**: `infrastructure/http/brapi/dto/BrapiQuoteResponse.java`

```java
public record BrapiQuoteResponse(
    List<BrapiQuoteResult> results,
    String requestedAt,
    String took
) {}

public record BrapiQuoteResult(
    String symbol,
    String shortName,
    String longName,
    String currency,
    Double regularMarketPrice,
    String marketCap,
    String logoUrl
) {}
```

#### 2.2 Cliente HTTP Brapi

**Arquivo**: `infrastructure/http/brapi/BrapiHttpClient.java`

```java
@Component
public class BrapiHttpClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String token;
    
    // Configuração de retry com backoff exponencial
    private final RetryConfig retryConfig = RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.ofMillis(200))
        .exponentialBackoffMultiplier(2.0)
        .build();
    
    public Mono<BrapiQuoteResponse> getQuote(String ticker) {
        // Implementação com HttpClient Java 11+
        // Timeout de 2s para conexão e leitura
        // Retry com jitter
    }
}
```

#### 2.3 Port para Classificação

**Arquivo**: `application/port/output/ClassificadorAtivoPort.java`

```java
public interface ClassificadorAtivoPort {
    Mono<ClassificacaoResult> classificar(String codigo);
}

public record ClassificacaoResult(
    String codigo,
    TipoAtivo categoria,
    FonteClassificacao fonte,
    String detalhes
) {}
```

### Fase 3: Serviço de Classificação

#### 3.1 Implementação do Classificador

**Arquivo**: `application/service/ClassificadorAtivoService.java`

```java
@Service
public class ClassificadorAtivoService implements ClassificadorAtivoPort {
    
    private final BrapiHttpClient brapiClient;
    
    public Mono<ClassificacaoResult> classificar(String codigo) {
        String normalizado = normalizar(codigo);
        
        // 1. Verificar se termina com 11
        if (normalizado.endsWith("11")) {
            return classificarCandidatoFiiEtfUnit(normalizado);
        }
        
        // 2. Se não termina com 11, é ação
        return Mono.just(new ClassificacaoResult(
            normalizado, 
            TipoAtivo.ACAO, 
            FonteClassificacao.HEURISTICA,
            "Código não termina com 11"
        ));
    }
    
    private Mono<ClassificacaoResult> classificarCandidatoFiiEtfUnit(String codigo) {
        return brapiClient.getQuote(codigo)
            .map(response -> analisarNomeBrapi(codigo, response))
            .onErrorReturn(criarResultadoHeuristico(codigo));
    }
    
    private ClassificacaoResult analisarNomeBrapi(String codigo, BrapiQuoteResponse response) {
        if (response.results().isEmpty()) {
            return criarResultadoHeuristico(codigo);
        }
        
        BrapiQuoteResult result = response.results().get(0);
        String nomeCompleto = (result.shortName() + " " + result.longName()).toUpperCase();
        
        // Regras de classificação por nome
        if (nomeCompleto.contains("FII")) {
            return new ClassificacaoResult(codigo, TipoAtivo.FII, FonteClassificacao.BRAPI, "Nome contém FII");
        }
        
        if (nomeCompleto.contains("UNT") || nomeCompleto.contains("UNIT")) {
            return new ClassificacaoResult(codigo, TipoAtivo.UNIT, FonteClassificacao.BRAPI, "Nome contém UNIT");
        }
        
        List<String> marcadoresEtf = List.of("ETF", "INDEX", "ÍNDICE", "FUNDO DE ÍNDICE", "ISHARES", "TREND");
        if (marcadoresEtf.stream().anyMatch(nomeCompleto::contains)) {
            return new ClassificacaoResult(codigo, TipoAtivo.ETF, FonteClassificacao.BRAPI, "Nome contém marcador ETF");
        }
        
        // Se não encontrou marcadores específicos, assume UNIT por heurística
        return new ClassificacaoResult(codigo, TipoAtivo.UNIT, FonteClassificacao.HEURISTICA, "Sufixo 11 sem marcadores específicos");
    }
}
```

### Fase 4: API Unificada

#### 4.1 DTO Unificado de Resposta

**Arquivo**: `adapter/input/web/dto/AtivoResponseDTO.java`

```java
public record AtivoResponseDTO(
    String codigo,
    TipoAtivo tipoAtivo,
    String nomeEmpresa,
    LocalDateTime dataAtualizacao,
    AcaoResponseDTO dadosAcao,  // null se for FII
    FiiResponseDTO dadosFii     // null se for ação
) {}
```

#### 4.2 Port de Caso de Uso Unificado

**Arquivo**: `application/port/input/AtivoUseCasePort.java`

```java
public interface AtivoUseCasePort {
    Mono<AtivoResponseDTO> obterAtivo(String codigo, boolean forceUpdate);
    Mono<ClassificacaoResult> classificarAtivo(String codigo);
}
```

#### 4.3 Serviço Unificado

**Arquivo**: `application/service/AtivoUseCaseService.java`

```java
@Service
public class AtivoUseCaseService implements AtivoUseCasePort {
    
    private final AcaoRepositoryPort acaoRepository;
    private final FiiRepositoryPort fiiRepository;
    private final ClassificadorAtivoService classificador;
    private final AcaoDataScrapperPort acaoScraper;
    private final FiiDataScrapperPort fiiScraper;
    
    @Override
    public Mono<AtivoResponseDTO> obterAtivo(String codigo, boolean forceUpdate) {
        String normalizado = normalizar(codigo);
        
        return buscarNosBancos(normalizado)
            .flatMap(resultado -> {
                if (resultado.isPresent()) {
                    return processarAtivoExistente(resultado.get(), forceUpdate);
                } else {
                    return criarNovoAtivo(normalizado);
                }
            });
    }
    
    private Mono<AtivoResponseDTO> criarNovoAtivo(String codigo) {
        return classificador.classificar(codigo)
            .flatMap(classificacao -> {
                switch (classificacao.categoria()) {
                    case FII -> criarEDispararScrapingFii(codigo, classificacao);
                    case ACAO, UNIT, ETF -> criarEDispararScrapingAcao(codigo, classificacao);
                    default -> Mono.error(new CodigoAtivoNaoEncontradoException(codigo));
                }
            });
    }
}
```

#### 4.4 Controller Unificado

**Arquivo**: `adapter/input/web/AtivoController.java`

```java
@RestController
@RequestMapping("/ativo")
public class AtivoController {
    
    private final AtivoUseCasePort useCase;
    
    @GetMapping("/{codigo}")
    public Mono<ResponseEntity<AtivoResponseDTO>> obterAtivo(
            @PathVariable String codigo,
            @RequestParam(defaultValue = "false") boolean force) {
        
        return useCase.obterAtivo(codigo, force)
            .map(ResponseEntity::ok)
            .onErrorResume(CodigoAtivoNaoEncontradoException.class, 
                e -> Mono.just(ResponseEntity.notFound().build()))
            .onErrorResume(RateLimitExceededException.class,
                e -> Mono.just(ResponseEntity.status(429).build()));
    }
}

@RestController
@RequestMapping("/classifica")
public class ClassificacaoController {
    
    private final AtivoUseCasePort useCase;
    
    @GetMapping("/{codigo}")
    public Mono<ResponseEntity<ClassificacaoResult>> classificar(@PathVariable String codigo) {
        return useCase.classificarAtivo(codigo)
            .map(ResponseEntity::ok);
    }
}
```

### Fase 5: Exceções e Configurações

#### 5.1 Nova Exceção

**Arquivo**: `domain/exception/CodigoAtivoNaoEncontradoException.java`

```java
public class CodigoAtivoNaoEncontradoException extends RuntimeException {
    public CodigoAtivoNaoEncontradoException(String codigo) {
        super("Código de ativo não encontrado: " + codigo);
    }
}
```

#### 5.2 Configuração do Cliente HTTP

**Arquivo**: `infrastructure/config/BrapiConfig.java`

```java
@Configuration
@ConfigurationProperties(prefix = "brapi")
public class BrapiConfig {
    private String baseUrl = "https://brapi.dev/api";
    private String token;
    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration readTimeout = Duration.ofSeconds(2);
    
    @Bean
    public HttpClient brapiHttpClient() {
        return HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .build();
    }
}
```

## 📋 Checklist de Implementação

### ✅ Preparação
- [ ] Criar migração Flyway para `tipo_codigo`
- [ ] Implementar enums `TipoAtivo` e `FonteClassificacao`
- [ ] Atualizar entidades JPA com nova coluna

### ✅ Integração Brapi
- [ ] Implementar DTOs da API Brapi
- [ ] Criar cliente HTTP com retry e timeout
- [ ] Implementar port `ClassificadorAtivoPort`
- [ ] Criar serviço de classificação com regras de negócio

### ✅ API Unificada
- [ ] Implementar DTO unificado `AtivoResponseDTO`
- [ ] Criar port `AtivoUseCasePort`
- [ ] Implementar serviço unificado `AtivoUseCaseService`
- [ ] Criar controllers para `/ativo` e `/classifica`

### ✅ Testes e Configuração
- [ ] Configurar propriedades da aplicação
- [ ] Implementar testes unitários
- [ ] Implementar testes de integração
- [ ] Documentar APIs no README

## 🔧 Configurações Necessárias

### application.yml

```yaml
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
```

## 🚨 Considerações Importantes

### Segurança
- **Nunca logar o token da Brapi**
- Usar variáveis de ambiente para configurações sensíveis
- Implementar rate limiting adequado

### Performance
- Cache de classificações por tempo limitado
- Scraping assíncrono para não bloquear resposta
- Índices no banco para `tipo_codigo`

### Resiliência
- Circuit breaker para API Brapi
- Fallback para heurística quando Brapi falhar
- Retry com backoff exponencial e jitter

### Compatibilidade
- Manter endpoints existentes funcionando
- Migração gradual dos dados existentes
- Versionamento da API se necessário

## 📈 Próximos Passos

1. **Validação do Plano**: Revisar com stakeholders
2. **Implementação Incremental**: Seguir fases definidas
3. **Testes Extensivos**: Validar cada componente
4. **Deploy Gradual**: Implementar feature flags se necessário
5. **Monitoramento**: Acompanhar métricas de uso e performance

---

**Data de Criação**: Janeiro 2025  
**Versão**: 1.0  
**Status**: Planejamento