-- V7__join_ativo_move_bdr_and_refactor_dividendo.sql
-- Migra V6 (bdr + dividendo) para JOINED com Ativo + FK em dividendo.

BEGIN;

-- =========================================================
-- 1) ATIVO (raiz JOINED) — espelha AtivoFinanceiroEntity
-- =========================================================
CREATE TABLE IF NOT EXISTS ativo (
                                     id                BIGSERIAL PRIMARY KEY,
                                     ticker            VARCHAR(20) NOT NULL UNIQUE,
    nome              VARCHAR(200),
    investidor_id     INTEGER,
    preco_atual       NUMERIC(19,6),
    variacao_12m      NUMERIC(19,6),
    dividend_yield    NUMERIC(19,6),
    data_atualizacao  TIMESTAMPTZ,
    tipo_ativo        VARCHAR(50) NOT NULL,
    dtype             VARCHAR(31) -- Discriminator (@DiscriminatorColumn)
    );
CREATE INDEX IF NOT EXISTS ix_ativo_data_atualizacao ON ativo (data_atualizacao);

-- =========================================================
-- 2) BACKFILL de BDR -> ATIVO, preservando IDs (JOINED)
--    Mapas:
--      ativo.ticker           <- bdr.ticker
--      ativo.nome             <- bdr.nome_bdr
--      ativo.investidor_id    <- bdr.investidor_id
--      ativo.preco_atual      <- bdr.cotacao
--      ativo.variacao_12m     <- bdr.variacao_12 (cast p/ numeric)
--      ativo.data_atualizacao <- bdr.updated_at
--      ativo.tipo_ativo       <- 'BDR'
--      ativo.dtype            <- 'BDR'
-- =========================================================
INSERT INTO ativo (id, ticker, nome, investidor_id, preco_atual, variacao_12m, dividend_yield, data_atualizacao, tipo_ativo, dtype)
    OVERRIDING SYSTEM VALUE
SELECT  b.id,
        b.ticker,
        b.nome_bdr,
        b.investidor_id,
        b.cotacao,
        NULLIF(b.variacao_12::text, '')::NUMERIC,
        NULL,                 -- não existia no V6
        b.updated_at,
        'BDR',
        'BDR'
FROM bdr b
    ON CONFLICT (id) DO NOTHING;

-- =========================================================
-- 3) JOINED: bdr.id -> ativo.id (PK=FK) e limpeza de colunas migradas
-- =========================================================
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_bdr_ativo') THEN
ALTER TABLE bdr
    ADD CONSTRAINT fk_bdr_ativo
        FOREIGN KEY (id) REFERENCES ativo(id) ON DELETE CASCADE;
END IF;
END$$;

-- Drop índices/colunas do bdr que migraram p/ ativo
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'ix_bdr_ticker') THEN
DROP INDEX ix_bdr_ticker;
END IF;
  IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'ix_bdr_updated_at') THEN
DROP INDEX ix_bdr_updated_at;
END IF;

  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='bdr' AND column_name='ticker') THEN
ALTER TABLE bdr DROP COLUMN ticker;
END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='bdr' AND column_name='nome_bdr') THEN
ALTER TABLE bdr DROP COLUMN nome_bdr;
END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='bdr' AND column_name='investidor_id') THEN
ALTER TABLE bdr DROP COLUMN investidor_id;
END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='bdr' AND column_name='cotacao') THEN
ALTER TABLE bdr DROP COLUMN cotacao;
END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='bdr' AND column_name='variacao_12') THEN
ALTER TABLE bdr DROP COLUMN variacao_12;
END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='bdr' AND column_name='updated_at') THEN
ALTER TABLE bdr DROP COLUMN updated_at;
END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='bdr' AND column_name='dividendo_json') THEN
ALTER TABLE bdr DROP COLUMN dividendo_json;
END IF;
END$$;

-- =========================================================
-- 4) DIVIDENDO — refatorar para PK simples + FK ativo_id
--     - PK antiga: (mes, tipo_dividendo)
--     - rename currency -> moeda
--     - add ativo_id (FK NOT NULL após popular, se necessário)
--     - unique(ativo_id, mes, tipo_dividendo, moeda)
-- =========================================================

-- Remover PK antiga, se existir
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'pk_dividendo') THEN
ALTER TABLE dividendo DROP CONSTRAINT pk_dividendo;
END IF;
END$$;

-- Adicionar colunas novas
ALTER TABLE dividendo
    ADD COLUMN IF NOT EXISTS id BIGSERIAL,
    ADD COLUMN IF NOT EXISTS ativo_id BIGINT;

-- Ajustar nome da coluna de moeda (currency -> moeda)
DO $$
BEGIN
  IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_name='dividendo' AND column_name='currency'
  ) AND NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_name='dividendo' AND column_name='moeda'
  ) THEN
ALTER TABLE dividendo RENAME COLUMN currency TO moeda;
END IF;
END$$;

-- PK simples
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'pk_dividendo_id') THEN
ALTER TABLE dividendo ADD CONSTRAINT pk_dividendo_id PRIMARY KEY (id);
END IF;
END$$;

-- FK p/ ativo (deixe NOT NULL só depois de popular, se tiver legado)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_dividendo_ativo') THEN
ALTER TABLE dividendo
    ADD CONSTRAINT fk_dividendo_ativo
        FOREIGN KEY (ativo_id) REFERENCES ativo(id) ON DELETE CASCADE;
END IF;
END$$;

-- Unique de negócio
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_dividendo_ativo_mes_tipo_moeda') THEN
ALTER TABLE dividendo
    ADD CONSTRAINT uk_dividendo_ativo_mes_tipo_moeda
        UNIQUE (ativo_id, mes, tipo_dividendo, moeda);
END IF;
END$$;

-- Índices úteis
CREATE INDEX IF NOT EXISTS ix_div_ativo_mes ON dividendo (ativo_id, mes);
CREATE INDEX IF NOT EXISTS ix_div_tipo ON dividendo (tipo_dividendo);

-- Checks (garantir presença)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_dividendo_mes_format') THEN
ALTER TABLE dividendo
    ADD CONSTRAINT ck_dividendo_mes_format
        CHECK (mes ~ '^[0-9]{4}-(0[1-9]|1[0-2])$');
END IF;
  -- currency -> moeda
  IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_dividendo_currency') THEN
ALTER TABLE dividendo DROP CONSTRAINT ck_dividendo_currency;
END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_dividendo_moeda') THEN
ALTER TABLE dividendo
    ADD CONSTRAINT ck_dividendo_moeda
        CHECK (moeda ~ '^[A-Z]{3}$');
END IF;
END$$;

COMMIT;
