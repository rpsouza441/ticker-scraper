-- Enums
DO $$ BEGIN
  CREATE TYPE quality_enum AS ENUM ('ok','missing','zero_real','unknown');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE paridade_method_enum AS ENUM ('SOURCE_HTML','SOURCE_B3','SOURCE_DEPOSITARY','INFERRED_PRICE');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Tabela principal (BDR)
DROP TABLE IF EXISTS bdr CASCADE;
CREATE TABLE bdr (
  id                   BIGSERIAL PRIMARY KEY,
  investidor_id        INT NOT NULL UNIQUE,                 -- id interno Investidor10
  ticker               VARCHAR(10) NOT NULL UNIQUE,
  tipo_ativo           VARCHAR(20) NOT NULL DEFAULT 'BDR_NAO_PATROCINADO',
  nome_bdr             VARCHAR(255),
  setor                VARCHAR(100),
  industria            VARCHAR(100),

  -- Moedas por seção
  price_currency       VARCHAR(3) NOT NULL DEFAULT 'BRL',   -- sempre BRL p/ BDR
  financials_currency  VARCHAR(3) NOT NULL DEFAULT 'USD',   -- DRE/BP/FC/market cap

  -- Preço/variação (preço em BRL)
  cotacao              NUMERIC(14,4),
  variacao_12m         NUMERIC(10,6),                       -- fração; pode ser negativa
  CONSTRAINT chk_bdr_variacao_12m_range CHECK (variacao_12m BETWEEN -1 AND 1),

  updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  CONSTRAINT chk_bdr_tipo_ativo CHECK (tipo_ativo IN ('BDR_PATROCINADO','BDR_NAO_PATROCINADO','DESCONHECIDO'))
);

CREATE INDEX idx_bdr_ticker         ON bdr(ticker);
CREATE INDEX idx_bdr_investidor_id  ON bdr(investidor_id);
CREATE INDEX idx_bdr_setor          ON bdr(setor);
CREATE INDEX idx_bdr_updated_at     ON bdr(updated_at);

-- Market cap (auditoria)
DROP TABLE IF EXISTS bdr_market_cap;
CREATE TABLE bdr_market_cap (
  bdr_id      BIGINT PRIMARY KEY REFERENCES bdr(id) ON DELETE CASCADE,
  value       NUMERIC(30,6),                     -- USD absoluto (trilhões cabem)
  currency    VARCHAR(3) NOT NULL DEFAULT 'USD',
  quality     quality_enum NOT NULL DEFAULT 'unknown',
  raw         TEXT                                -- "US$ 3,66 Trilhões"
);

-- Paridade BDR (auditoria)
DROP TABLE IF EXISTS bdr_paridade;
CREATE TABLE bdr_paridade (
  bdr_id            BIGINT PRIMARY KEY REFERENCES bdr(id) ON DELETE CASCADE,
  value             INT,                               -- ex.: 20 = 1 Stock = 20 BDRs
  method            paridade_method_enum,
  confidence        NUMERIC(4,3),                      -- 0..1 (só p/ INFERRED_PRICE)
  last_verified_at  TIMESTAMPTZ,
  raw               TEXT                               -- "1 Stock = 20 BDRs"
);

-- Série de preços (BRL)
DROP TABLE IF EXISTS bdr_price_series;
CREATE TABLE bdr_price_series (
  id        BIGSERIAL PRIMARY KEY,
  bdr_id    BIGINT NOT NULL REFERENCES bdr(id) ON DELETE CASCADE,
  dt        DATE   NOT NULL,
  close     NUMERIC(14,4) NOT NULL                  -- BRL
);
CREATE UNIQUE INDEX uq_bdr_price_series ON bdr_price_series(bdr_id, dt);
CREATE INDEX idx_bdr_price_series_dt ON bdr_price_series(dt);

-- Dividendos anuais (normalmente USD)
DROP TABLE IF EXISTS bdr_dividends_yearly;
CREATE TABLE bdr_dividends_yearly (
  id        BIGSERIAL PRIMARY KEY,
  bdr_id    BIGINT NOT NULL REFERENCES bdr(id) ON DELETE CASCADE,
  year      INT NOT NULL,
  valor     NUMERIC(18,6),
  currency  VARCHAR(3) NOT NULL DEFAULT 'USD'
);
CREATE UNIQUE INDEX uq_bdr_div_year ON bdr_dividends_yearly(bdr_id, year);

-- Indicadores atuais (razões, frações onde aplicável)
DROP TABLE IF EXISTS bdr_indicators_current;
CREATE TABLE bdr_indicators_current (
  bdr_id  BIGINT PRIMARY KEY REFERENCES bdr(id) ON DELETE CASCADE,
  pl NUMERIC(18,6), pvp NUMERIC(18,6), psr NUMERIC(18,6),
  p_ebit NUMERIC(18,6), p_ebitda NUMERIC(18,6), p_ativo NUMERIC(18,6),
  roe NUMERIC(18,6), roic NUMERIC(18,6), roa NUMERIC(18,6),
  margem_bruta NUMERIC(18,6), margem_operacional NUMERIC(18,6), margem_liquida NUMERIC(18,6),
  vpa NUMERIC(18,6), lpa NUMERIC(18,6), patrimonio_por_ativos NUMERIC(18,6)
);

-- Indicadores históricos (≤ 5 anos)
DROP TABLE IF EXISTS bdr_indicators_history;
CREATE TABLE bdr_indicators_history (
  id      BIGSERIAL PRIMARY KEY,
  bdr_id  BIGINT NOT NULL REFERENCES bdr(id) ON DELETE CASCADE,
  year    INT NOT NULL,
  pl NUMERIC(18,6), pvp NUMERIC(18,6), psr NUMERIC(18,6),
  p_ebit NUMERIC(18,6), p_ebitda NUMERIC(18,6), p_ativo NUMERIC(18,6),
  roe NUMERIC(18,6), roic NUMERIC(18,6), roa NUMERIC(18,6),
  margem_bruta NUMERIC(18,6), margem_operacional NUMERIC(18,6), margem_liquida NUMERIC(18,6),
  vpa NUMERIC(18,6), lpa NUMERIC(18,6), patrimonio_por_ativos NUMERIC(18,6)
);
CREATE UNIQUE INDEX uq_bdr_ind_hist ON bdr_indicators_history(bdr_id, year);

-- DRE (USD) com qualidade por campo
DROP TABLE IF EXISTS bdr_dre_yearly;
CREATE TABLE bdr_dre_yearly (
  id                 BIGSERIAL PRIMARY KEY,
  bdr_id             BIGINT NOT NULL REFERENCES bdr(id) ON DELETE CASCADE,
  year               INT NOT NULL,

  receita_total_val  NUMERIC(30,6),
  receita_total_qual quality_enum DEFAULT 'unknown',
  receita_total_raw  TEXT,

  lucro_bruto_val    NUMERIC(30,6),
  lucro_bruto_qual   quality_enum DEFAULT 'unknown',
  lucro_bruto_raw    TEXT,

  ebitda_val         NUMERIC(30,6),
  ebitda_qual        quality_enum DEFAULT 'unknown',
  ebitda_raw         TEXT,

  ebit_val           NUMERIC(30,6),
  ebit_qual          quality_enum DEFAULT 'unknown',
  ebit_raw           TEXT,

  lucro_liquido_val  NUMERIC(30,6),
  lucro_liquido_qual quality_enum DEFAULT 'unknown',
  lucro_liquido_raw  TEXT
);
CREATE UNIQUE INDEX uq_bdr_dre_year ON bdr_dre_yearly(bdr_id, year);

-- Balanço patrimonial (USD) com qualidade
DROP TABLE IF EXISTS bdr_bp_yearly;
CREATE TABLE bdr_bp_yearly (
  id                     BIGSERIAL PRIMARY KEY,
  bdr_id                 BIGINT NOT NULL REFERENCES bdr(id) ON DELETE CASCADE,
  year                   INT NOT NULL,

  ativos_totais_val      NUMERIC(30,6),
  ativos_totais_qual     quality_enum DEFAULT 'unknown',
  ativos_totais_raw      TEXT,

  passivos_totais_val    NUMERIC(30,6),
  passivos_totais_qual   quality_enum DEFAULT 'unknown',
  passivos_totais_raw    TEXT,

  divida_lp_val          NUMERIC(30,6),
  divida_lp_qual         quality_enum DEFAULT 'unknown',
  divida_lp_raw          TEXT,

  pl_val                 NUMERIC(30,6),
  pl_qual                quality_enum DEFAULT 'unknown',
  pl_raw                 TEXT
);
CREATE UNIQUE INDEX uq_bdr_bp_year ON bdr_bp_yearly(bdr_id, year);

-- Fluxo de caixa (USD) com qualidade
DROP TABLE IF EXISTS bdr_fc_yearly;
CREATE TABLE bdr_fc_yearly (
  id              BIGSERIAL PRIMARY KEY,
  bdr_id          BIGINT NOT NULL REFERENCES bdr(id) ON DELETE CASCADE,
  year            INT NOT NULL,

  fco_val         NUMERIC(30,6),
  fco_qual        quality_enum DEFAULT 'unknown',
  fco_raw         TEXT,

  fci_val         NUMERIC(30,6),
  fci_qual        quality_enum DEFAULT 'unknown',
  fci_raw         TEXT,

  fcf_val         NUMERIC(30,6),
  fcf_qual        quality_enum DEFAULT 'unknown',
  fcf_raw         TEXT
);
CREATE UNIQUE INDEX uq_bdr_fc_year ON bdr_fc_yearly(bdr_id, year);

-- Comentários básicos
COMMENT ON TABLE bdr IS 'BDRs raspados (preço BRL; financeiros USD).';
COMMENT ON COLUMN bdr.price_currency      IS 'Moeda da cotação/série.';
COMMENT ON COLUMN bdr.financials_currency IS 'Moeda de DRE/BP/FC e market cap.';
COMMENT ON TABLE bdr_market_cap IS 'Market cap com auditoria (valor, moeda, qualidade, raw).';
COMMENT ON TABLE bdr_paridade IS 'Paridade BDR (valor + método + confiança + raw).';
