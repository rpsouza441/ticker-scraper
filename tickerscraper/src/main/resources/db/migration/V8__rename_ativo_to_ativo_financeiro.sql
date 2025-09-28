-- V8__rename_ativo_to_ativo_financeiro.sql
-- Renomeia a tabela 'ativo' -> 'ativo_financeiro' e ajusta FKs/índices/sequence

BEGIN;

DO $$
BEGIN
  -- Só executa se a tabela 'ativo' existir
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'ativo'
  ) THEN

    -- 1) Remover FKs que apontam para 'ativo'
    IF EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE table_schema = 'public' AND table_name = 'bdr' AND constraint_name = 'fk_bdr_ativo'
    ) THEN
ALTER TABLE public.bdr DROP CONSTRAINT fk_bdr_ativo;
END IF;

    IF EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE table_schema = 'public' AND table_name = 'dividendo' AND constraint_name = 'fk_dividendo_ativo'
    ) THEN
ALTER TABLE public.dividendo DROP CONSTRAINT fk_dividendo_ativo;
END IF;

    -- 2) Renomear a tabela
ALTER TABLE public.ativo RENAME TO ativo_financeiro;

-- 3) Renomear a sequence padrão (caso tenha sido criada como BIGSERIAL)
IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'ativo_id_seq') THEN
ALTER SEQUENCE public.ativo_id_seq RENAME TO ativo_financeiro_id_seq;
ALTER TABLE public.ativo_financeiro
    ALTER COLUMN id SET DEFAULT nextval('public.ativo_financeiro_id_seq');
END IF;

    -- 4) Renomear índices
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'ux_ativo_ticker') THEN
      ALTER INDEX public.ux_ativo_ticker RENAME TO ux_ativo_financeiro_ticker;
END IF;

    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'ix_ativo_data_atualizacao') THEN
      ALTER INDEX public.ix_ativo_data_atualizacao RENAME TO ix_ativo_financeiro_data_atualizacao;
END IF;

    -- 5) Recriar FKs apontando para 'ativo_financeiro'
    -- BDR (herança JOINED: PK de bdr referencia PK do pai)
ALTER TABLE public.bdr
    ADD CONSTRAINT fk_bdr_ativo
        FOREIGN KEY (id) REFERENCES public.ativo_financeiro(id) ON DELETE CASCADE;

-- Dividendo -> AtivoFinanceiro (FK simples)
-- Garante que a coluna exista antes de criar a FK
IF EXISTS (
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name = 'dividendo' AND column_name = 'ativo_id'
    ) THEN
ALTER TABLE public.dividendo
    ADD CONSTRAINT fk_dividendo_ativo
        FOREIGN KEY (ativo_id) REFERENCES public.ativo_financeiro(id) ON DELETE CASCADE;
END IF;

END IF;
END
$$;

COMMIT;
