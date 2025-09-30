-- V10__increase_precision_bdr_numeric_fields.sql
-- Aumenta a precisão dos campos numéricos da tabela BDR para evitar overflow
-- Altera de precision=19,scale=6 para precision=28,scale=6

-- Campos de indicadores financeiros
ALTER TABLE bdr ALTER COLUMN pl TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN pvp TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN psr TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN p_ebit TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN p_ebitda TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN p_ativo TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN roe TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN roic TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN roa TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN margem_bruta TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN margem_operacional TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN margem_liquida TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN vpa TYPE NUMERIC(28,6);
ALTER TABLE bdr ALTER COLUMN lpa TYPE NUMERIC(28,6);

-- Campo de paridade
ALTER TABLE bdr ALTER COLUMN paridade_ratio TYPE NUMERIC(28,6);