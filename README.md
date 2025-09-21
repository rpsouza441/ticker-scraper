# Ticker Scraper

## 📋 Descrição

O **Ticker Scraper** é uma aplicação Spring Boot desenvolvida para realizar scraping de dados de **Fundos de Investimento Imobiliário (FIIs)**, **Ações**, **ETFs** e **BDRs** do mercado financeiro brasileiro. A aplicação utiliza arquitetura hexagonal (ports and adapters) e oferece APIs REST para consulta de dados financeiros estruturados, incluindo um **endpoint unificado** que detecta automaticamente o tipo de ativo.

## 🏗️ Arquitetura

O projeto segue os princípios da **Arquitetura Hexagonal**, organizando o código em camadas bem definidas:

```
├── domain/           # Regras de negócio e entidades
│   ├── model/        # Entidades de domínio (FundoImobiliario, Acao)
│   └── exception/    # Exceções específicas do domínio
├── application/      # Casos de uso e lógica de aplicação
│   ├── port/         # Interfaces (input/output ports)
│   ├── service/      # Implementação dos casos de uso
│   └── dto/          # Objetos de transferência de dados
├── adapter/          # Adaptadores para mundo externo
│   ├── input/web/    # Controllers REST
│   └── output/       # Adaptadores de persistência
└── infrastructure/   # Configurações e implementações técnicas
    ├── config/       # Configurações Spring
    ├── scraper/      # Implementações de scraping
    └── parser/       # Parsers de dados
```

## 🚀 Tecnologias Utilizadas

### Core
- **Java 21** - Linguagem de programação
- **Spring Boot 3.5.3** - Framework principal
- **Spring WebFlux** - Programação reativa
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL** - Banco de dados
- **Flyway** - Migração de banco de dados

### Scraping
- **Selenium WebDriver 4.34.0** - Automação de navegador
- **Microsoft Playwright 1.44.0** - Alternativa moderna para automação
- **WebDriverManager 6.1.0** - Gerenciamento automático de drivers
- **JSoup 1.21.1** - Parser HTML

### Resiliência e Monitoramento
- **Resilience4j** - Circuit breaker, retry e timeout
- **Spring Boot Actuator** - Monitoramento e métricas
- **Micrometer** - Métricas e observabilidade

### Utilitários
- **Lombok** - Redução de boilerplate
- **MapStruct** - Mapeamento entre objetos

## 📊 Funcionalidades

### APIs Disponíveis

#### 🎯 Endpoint Unificado (Recomendado)
- `GET /api/v1/ticker/{ticker}` - **Endpoint unificado para qualquer tipo de ativo**
  - Detecta automaticamente o tipo (Ação, FII, ETF, BDR)
  - Retorna dados estruturados no formato unificado
  - Suporte a cache inteligente e circuit breaker
- `GET /api/v1/ticker/{ticker}/classificacao` - Classificação do tipo de ativo

#### Fundos de Investimento Imobiliário (FIIs)
- `GET /fii/get-{ticker}` - Dados processados de um FII
- `GET /fii/get-{ticker}/raw` - Dados brutos de um FII

#### Ações
- `GET /acao/get-{ticker}` - Dados processados de uma ação
- `GET /acao/get-{ticker}/raw` - Dados brutos de uma ação

### Dados Coletados

#### Para FIIs:
- Informações básicas (ticker, nome, razão social, CNPJ)
- Dados financeiros (cotação, dividend yield, P/VP)
- Informações do fundo (segmento, mandato, taxa de administração)
- Histórico de dividendos
- Métricas de performance

#### Para Ações:
- Informações básicas (ticker, empresa, setor)
- Indicadores financeiros (P/L, P/VP, ROE, ROIC)
- Dados de mercado (cotação, volume, liquidez)
- Métricas de rentabilidade e endividamento

## ⚙️ Configuração

### Pré-requisitos
- Java 21+
- Maven 3.11.0+
- PostgreSQL
- Chrome/Chromium (para Selenium)

### Variáveis de Ambiente

```bash
# Servidor
SERVER_PORT=8080

# Banco de Dados
DB_URL=jdbc:postgresql://localhost:5432/tickerscraper
DB_USER=seu_usuario
DB_PASS=sua_senha
DB_POOL_SIZE=10

# Hibernate
HIBERNATE_DDL=validate
HIBERNATE_SHOW_SQL=false

# Logs
ROOT_LOG_LEVEL=INFO
LOG_FILE_PATH=logs/tickerscraper.log

# Scraper
SCRAPER_NETWORK_CAPTURE_TIMEOUT_MS=10000
SELENIUM_WEBDRIVER_MAX_ATTEMPTS=3
SELENIUM_PAGELOAD_MAX_ATTEMPTS=5
```

## 🔧 Instalação e Execução

### 1. Clone o repositório
```bash
git clone <url-do-repositorio>
cd ticker-scraper
```

### 2. Configure o banco de dados
```sql
CREATE DATABASE tickerscraper;
```

### 3. Configure as variáveis de ambiente
Crie um arquivo `.env` ou configure as variáveis no seu sistema.

### 4. Execute a aplicação
```bash
# Via Maven
./mvnw spring-boot:run

# Ou compile e execute o JAR
./mvnw clean package
java -jar target/tickerscraper-0.0.1-SNAPSHOT.jar
```

### 5. Acesse a aplicação
- API: `http://localhost:8080`
- Health Check: `http://localhost:8080/actuator/health`
- Métricas: `http://localhost:8080/actuator/metrics`

## 📝 Exemplos de Uso

### 🎯 Endpoint Unificado (Recomendado)
```bash
# Consultar qualquer tipo de ativo (detecta automaticamente)
curl -X GET "http://localhost:8080/api/v1/ticker/PETR4"
curl -X GET "http://localhost:8080/api/v1/ticker/HGLG11"
curl -X GET "http://localhost:8080/api/v1/ticker/BOVA11"

# Verificar apenas a classificação do ativo
curl -X GET "http://localhost:8080/api/v1/ticker/PETR4/classificacao"
```

### Consultar dados de um FII
```bash
curl -X GET "http://localhost:8080/fii/get-HGLG11"
```

### Consultar dados brutos de uma ação
```bash
curl -X GET "http://localhost:8080/acao/get-PETR4/raw"
```

## 🛡️ Resiliência

A aplicação implementa padrões de resiliência:

- **Circuit Breaker**: Previne cascata de falhas
- **Retry**: Tentativas automáticas em caso de falha temporária
- **Timeout**: Limita tempo de execução das operações
- **Rate Limiting**: Controla frequência de requisições

## 🔍 Monitoramento

### Endpoints de Monitoramento
- `/actuator/health` - Status da aplicação
- `/actuator/metrics` - Métricas da aplicação
- `/actuator/circuitbreakers` - Status dos circuit breakers

### Logs
A aplicação gera logs estruturados com correlation ID para rastreabilidade.

## 🚨 Tratamento de Erros

A aplicação trata diversos cenários de erro:

- **TickerNotFoundException**: Ticker não encontrado
- **AntiBotDetectedException**: Detecção de bot pelo site
- **ScrapingTimeoutException**: Timeout durante scraping
- **RateLimitExceededException**: Limite de requisições excedido
- **DataParsingException**: Erro no parsing dos dados

## 🤝 Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença especificada no arquivo `LICENSE`.

## 👨‍💻 Autor

**Rodrigo Pinheiro**
- Grupo: `br.dev.rodrigopinheiro`
- Projeto: Aplicativo que realiza scraper de ticker de FII e ação

---

⚠️ **Aviso Legal**: Este projeto é destinado apenas para fins educacionais e de pesquisa. Respeite os termos de uso dos sites de onde os dados são coletados e considere implementar delays apropriados entre requisições para evitar sobrecarga nos servidores.