-- ================================
-- Adição da coluna tipo_ativo e classificação de registros
-- ================================

-- Adicionar coluna tipo_ativo na tabela acao se não existir
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'acao' AND column_name = 'tipo_ativo'
    ) THEN
        ALTER TABLE acao ADD COLUMN tipo_ativo VARCHAR(20) DEFAULT 'DESCONHECIDO';
    END IF;
END $$;

-- Adicionar coluna tipo_ativo na tabela fundo_imobiliario se não existir
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'fundo_imobiliario' AND column_name = 'tipo_ativo'
    ) THEN
        ALTER TABLE fundo_imobiliario ADD COLUMN tipo_ativo VARCHAR(20) DEFAULT 'DESCONHECIDO';
    END IF;
END $$;

-- ================================
-- Classificação de tipos de ativo
-- ================================

-- Atualizar registros existentes com valores padrão baseados no ticker
-- Classificação por sufixo do ticker para ações
UPDATE acao SET tipo_ativo = 'ACAO_ON' WHERE tipo_ativo = 'DESCONHECIDO' AND ticker LIKE '%3';
UPDATE acao SET tipo_ativo = 'ACAO_PN' WHERE tipo_ativo = 'DESCONHECIDO' AND ticker LIKE '%4';
UPDATE acao SET tipo_ativo = 'ACAO_PNA' WHERE tipo_ativo = 'DESCONHECIDO' AND ticker LIKE '%5';
UPDATE acao SET tipo_ativo = 'ACAO_PNB' WHERE tipo_ativo = 'DESCONHECIDO' AND ticker LIKE '%6';
UPDATE acao SET tipo_ativo = 'ACAO_PND' WHERE tipo_ativo = 'DESCONHECIDO' AND ticker LIKE '%8';
UPDATE acao SET tipo_ativo = 'ACAO_UNIT' WHERE tipo_ativo = 'DESCONHECIDO' AND ticker LIKE '%11';
UPDATE acao SET tipo_ativo = 'ACAO_ON' WHERE tipo_ativo = 'DESCONHECIDO'; -- fallback para outros casos

-- Classificação para fundos imobiliários
UPDATE fundo_imobiliario SET tipo_ativo = 'FII' WHERE tipo_ativo = 'DESCONHECIDO';

-- Adicionar constraints NOT NULL após a classificação
ALTER TABLE acao ALTER COLUMN tipo_ativo SET NOT NULL;
ALTER TABLE fundo_imobiliario ALTER COLUMN tipo_ativo SET NOT NULL;

-- Comentários para documentação
COMMENT ON COLUMN acao.tipo_ativo IS 'Tipo específico da ação (ON, PN, PNA, PNB, PND, UNIT)';
COMMENT ON COLUMN fundo_imobiliario.tipo_ativo IS 'Tipo do fundo imobiliário (sempre FII)';