-- Migration V5: Criação da tabela ETF
-- Autor: Sistema de Scraping
-- Data: 2024
-- Descrição: Cria tabela para armazenar dados de ETFs (Exchange Traded Funds)

CREATE TABLE etf (
    id BIGSERIAL PRIMARY KEY,
    ticker VARCHAR(20) UNIQUE NOT NULL,
    tipo_ativo VARCHAR(20) NOT NULL DEFAULT 'ETF',
    nome_etf VARCHAR(255),
    valor_atual DECIMAL(15,2),
    capitalizacao DECIMAL(20,2),
    variacao_12m DECIMAL(10,4),
    variacao_60m DECIMAL(10,4),
    dy DECIMAL(10,4),
    dados_brutos_json JSONB,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para otimização de consultas
CREATE INDEX idx_etf_ticker ON etf(ticker);
CREATE INDEX idx_etf_tipo_ativo ON etf(tipo_ativo);
CREATE INDEX idx_etf_data_atualizacao ON etf(data_atualizacao);

-- Comentários para documentação
COMMENT ON TABLE etf IS 'Tabela para armazenar dados de ETFs (Exchange Traded Funds)';
COMMENT ON COLUMN etf.ticker IS 'Código do ETF na bolsa (ex: BOVA11)';
COMMENT ON COLUMN etf.tipo_ativo IS 'Tipo do ativo, sempre ETF para esta tabela';
COMMENT ON COLUMN etf.nome_etf IS 'Nome completo do ETF';
COMMENT ON COLUMN etf.valor_atual IS 'Valor atual da cota do ETF';
COMMENT ON COLUMN etf.capitalizacao IS 'Capitalização de mercado do ETF';
COMMENT ON COLUMN etf.variacao_12m IS 'Variação percentual em 12 meses';
COMMENT ON COLUMN etf.variacao_60m IS 'Variação percentual em 60 meses';
COMMENT ON COLUMN etf.dy IS 'Dividend Yield do ETF';
COMMENT ON COLUMN etf.dados_brutos_json IS 'Dados brutos em formato JSON para auditoria';
COMMENT ON COLUMN etf.data_atualizacao IS 'Data e hora da última atualização dos dados';