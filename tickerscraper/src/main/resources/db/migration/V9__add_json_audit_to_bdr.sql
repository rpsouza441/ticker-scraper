-- V9__add_json_audit_to_bdr.sql
-- Adiciona a coluna para auditoria de dados brutos na tabela de BDRs.

-- Usamos um bloco anônimo para garantir que a coluna seja adicionada apenas se não existir.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'bdr' AND column_name = 'dados_brutos_json'
    ) THEN
ALTER TABLE bdr ADD COLUMN dados_brutos_json JSONB;
COMMENT ON COLUMN bdr.dados_brutos_json IS 'Dados brutos em formato JSONB para fins de auditoria e reprocessamento.';
END IF;
END $$;