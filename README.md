# Ticker Scraper

## 📋 Descrição

O **Ticker Scraper** é uma aplicação **Spring Boot** para scraping de dados de **Fundos de Investimento Imobiliário (FIIs)**, **Ações**, **ETFs** e **BDRs** do mercado financeiro brasileiro. A aplicação utiliza **arquitetura hexagonal (Ports & Adapters)** e expõe **APIs REST** para consulta de dados financeiros estruturados, incluindo um **endpoint unificado** que detecta automaticamente o tipo de ativo.

A classificação de ativos **cache** e a **API externa Brapi** para resolver tickers ambíguos.

---

## 🏗️ Arquitetura

```
├── domain/                 # Regras de negócio e entidades
│   ├── model/              # Entidades de domínio (FundoImobiliario, Acao, Etf, Bdr...)
│   └── exception/          # Exceções específicas do domínio
├── application/            # Casos de uso e lógica de aplicação
│   ├── port/               # Interfaces (input/output ports)
│   ├── service/            # Implementação dos casos de uso
│   └── dto/                # Objetos de transferência de dados
├── adapter/                # Adaptadores para mundo externo
│   ├── input/web/          # Controllers REST
│   └── output/             # Persistência (JPA), HTTP clients, etc.
└── infrastructure/         # Configurações e implementações técnicas
    ├── config/             # Configurações Spring
    ├── scraper/            # Implementações de scraping (Playwright/Selenium/JSoup)
    └── http/               # Clientes HTTP para APIs externas (Brapi etc.)
```

---

## 🚀 Tecnologias Utilizadas

### Core
- **Java 21**
- **Spring Boot 3.5.3**
- **Spring WebFlux** – programação reativa
- **Spring Data JPA** – persistência de dados
- **PostgreSQL** – banco de dados
- **Flyway** – migrações de banco

### Scraping
- **Microsoft Playwright 1.44.0** – automação de navegador (preferencial)
- **Selenium WebDriver 4.34.0** – fallback
- **JSoup 1.21.1** – parser HTML

### Resiliência e Monitoramento
- **Resilience4j** – circuit breaker, retry, timeout
- **Spring Boot Actuator**
- **Micrometer** – métricas e observabilidade

### Utilitários
- **Lombok** – redução de boilerplate
- **MapStruct** – mapeamento entre objetos
- **Caffeine** – cache de alta performance
- **Brapi API** – classificação de ativos ambíguos

---

## 📊 Funcionalidades

### APIs Disponíveis

#### 🎯 Endpoint Unificado (Recomendado)
- `GET /api/v1/ticker/{ticker}` – **um endpoint para qualquer tipo de ativo** (Ação, FII, ETF, BDR).
  - Detecta automaticamente o tipo.
  - Retorna dados estruturados em **formato unificado**.
  - Usa **cache** e **circuit breaker**.
- `GET /api/v1/ticker/{ticker}/classificacao` – retorna apenas a **classificação** do ticker (debug/observabilidade).

#### Endpoints Específicos (Alternativos)
- **FIIs**
  - `GET /fii/get-{ticker}` – dados processados do FII.
  - `GET /fii/get-{ticker}/raw` – dados **brutos** do FII (inclui URLs de APIs capturadas).
- **Ações**
  - `GET /acao/get-{ticker}` – dados processados da ação.
  - `GET /acao/get-{ticker}/raw` – dados **brutos** da ação.
- **ETFs**
  - `GET /etf/get-{ticker}` – dados processados do ETF.
  - `GET /etf/get-{ticker}/raw` – dados **brutos** do ETF.

> Obs.: substitua `{ticker}` pelo código do ativo, por exemplo `HGLG11`, `PETR4`, `BOVA11`.

---

## 📦 Dados Coletados

### FIIs
- **Básico**: ticker, nome, razão social, CNPJ
- **Financeiro**: cotação, dividend yield, P/VP, liquidez
- **Fundo**: segmento, mandato, taxas
- **Histórico de dividendos**

### Ações
- **Básico**: ticker, empresa, setor
- **Fundamentalistas**: P/L, P/VP, ROE, ROIC, margens
- **Mercado**: cotação, valor de mercado, liquidez
- **Estrutura de capital**: endividamento e rentabilidade

### ETFs
- **Básico**: ticker, nome do ETF
- **Mercado**: preço/valor atual, capitalização
- **Performance**: variação 12M/60M, dividend yield

---

## ⚙️ Configuração

### Pré-requisitos
- **Java 21+**
- **Maven 3.11.0+**
- **PostgreSQL**
- **Chrome/Chromium** instalado

### Variáveis de Ambiente

```bash
# Servidor
SERVER_PORT=8080

# Banco de Dados
DB_URL=jdbc:postgresql://localhost:5432/tickerscraper
DB_USER=seu_usuario
DB_PASS=sua_senha
DB_POOL_SIZE=10

# Scraper
SCRAPER_NETWORK_CAPTURE_TIMEOUT_MS=10000

# API Externa (Brapi) – opcional (classificação de tickers ambíguos)
BRAPI_BASE_URL=https://brapi.dev/api
BRAPI_TOKEN=seu_token_aqui
```

---

## 🔧 Instalação e Execução

1) **Clone o repositório**
```bash
git clone <url-do-repositorio>
cd ticker-scraper
```

2) **Crie o banco de dados**
```sql
CREATE DATABASE tickerscraper;
```

3) **Configure as variáveis de ambiente**
- Use um arquivo `.env` ou configure no sistema operacional.

4) **Execute a aplicação**
```bash
# Via Maven (modo desenvolvimento)
./mvnw spring-boot:run

# Ou compile e execute o JAR
./mvnw clean package
java -jar target/tickerscraper-0.0.1-SNAPSHOT.jar
```

5) **Acesse**
- API: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- Métricas: `http://localhost:8080/actuator/metrics`

---

## 🐳 Executando com Docker

1) **Configure o ambiente**
   - Copie o arquivo de exemplo:
     ```bash
     cp docker/.env.example docker/.env
     ```
   - (Opcional) Edite o arquivo `docker/.env` com suas configurações.

2) **Execute com Docker Compose**
   ```bash
   cd docker
   docker compose up --build
   ```

3) **Acesse**
   - API: `http://localhost:8080`
   - O banco de dados PostgreSQL estará acessível na porta `5432`.

---

## 📝 Exemplos de Uso

### 🎯 Endpoint Unificado (recomendado)
```bash
# Detecta automaticamente o tipo de ativo
curl -X GET "http://localhost:8080/api/v1/ticker/PETR4"
curl -X GET "http://localhost:8080/api/v1/ticker/HGLG11"
curl -X GET "http://localhost:8080/api/v1/ticker/BOVA11"

# Apenas a classificação
curl -X GET "http://localhost:8080/api/v1/ticker/SAPR11/classificacao"
```

### Endpoints específicos
```bash
# FII (processado)
curl -X GET "http://localhost:8080/fii/get-HGLG11"

# Ação (bruto)
curl -X GET "http://localhost:8080/acao/get-PETR4/raw"
```

---

## 🛡️ Resiliência

- **Circuit Breaker**: evita cascata de falhas quando o alvo está instável.
- **Retry**: novas tentativas em falhas transitórias.
- **Timeout**: limite de tempo de scraping.
- **Fallback**: **Selenium** como alternativa quando **Playwright** falhar repetidamente.

---

## 🔍 Monitoramento

### Endpoints
- `/actuator/health` – status da aplicação
- `/actuator/metrics` – métricas
- `/actuator/circuitbreakers` – status dos circuit breakers

### Logs
- Logs estruturados com **correlationId** para rastreabilidade ponta a ponta.

---

## 🚨 Tratamento de Erros (exemplos)

- `TickerNotFoundException` – ticker não encontrado
- `AntiBotDetectedException` – bloqueio/antibot detectado
- `ScrapingTimeoutException` – timeout durante o scraping
- `DataParsingException` – falha ao parsear HTML
- `WebDriverInitializationException` – erro ao iniciar o navegador

---

## 🤝 Contribuição

1. Faça um **fork**
2. Crie sua branch: `git checkout -b feature/nova-feature`
3. Commit: `git commit -m "feat: adiciona nova feature"`
4. Push: `git push origin feature/nova-feature`
5. Abra um **Pull Request**

---

## 📄 Licença

Este projeto está sob a **licença MIT**.

---

## 👨‍💻 Autor

**Rodrigo Pinheiro**  
**Grupo**: `br.dev.rodrigopinheiro`  
**Projeto**: Aplicativo que realiza scraper de ticker de FII e ação

---

## ⚠️ Aviso Legal

Este projeto é destinado apenas a **fins educacionais e de pesquisa**. Respeite os **termos de uso** dos sites de onde os dados são coletados e **implemente atrasos** apropriados entre requisições para evitar sobrecarga nos servidores.
