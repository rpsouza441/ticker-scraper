-- ================================
-- Correção da constraint única para fii_dividendo
-- ================================

-- Remove a constraint existente se houver (pode ter nome diferente)
DO $$
BEGIN
    -- Tenta remover constraints existentes com nomes possíveis
    BEGIN
        ALTER TABLE fii_dividendo DROP CONSTRAINT IF EXISTS uk_fii_dividendo_fundo_mes;
    EXCEPTION
        WHEN OTHERS THEN NULL;
    END;
    
    BEGIN
        ALTER TABLE fii_dividendo DROP CONSTRAINT IF EXISTS fii_dividendo_fundo_imobiliario_id_mes_key;
    EXCEPTION
        WHEN OTHERS THEN NULL;
    END;
END $$;

-- Remover duplicatas existentes (manter apenas a mais recente por fundo/mês)
DELETE FROM fii_dividendo 
WHERE id NOT IN (
    SELECT DISTINCT ON (fundo_imobiliario_id, mes) id
    FROM fii_dividendo 
    ORDER BY fundo_imobiliario_id, mes, id DESC
);

-- Adicionar a constraint única correta
ALTER TABLE fii_dividendo 
ADD CONSTRAINT uk_fii_dividendo_fundo_mes 
UNIQUE (fundo_imobiliario_id, mes);

-- Comentário para documentação
COMMENT ON CONSTRAINT uk_fii_dividendo_fundo_mes ON fii_dividendo IS 
'Garante que não existam dividendos duplicados para o mesmo fundo no mesmo mês';