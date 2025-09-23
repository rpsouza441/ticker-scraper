-- ================================
-- tabela: fundo_imobiliario
-- ================================
CREATE TABLE IF NOT EXISTS fundo_imobiliario (
                                                 id                           BIGSERIAL PRIMARY KEY,
                                                 internal_id                  BIGINT NOT NULL UNIQUE,
                                                 ticker                       VARCHAR(20) NOT NULL UNIQUE,

    nome_empresa                 VARCHAR(255),
    razao_social                 VARCHAR(255),
    cnpj                         VARCHAR(32),
    publico_alvo                 VARCHAR(128),
    mandato                      VARCHAR(128),
    segmento                     VARCHAR(128),
    tipo_de_fundo                VARCHAR(128),
    prazo_de_duracao             VARCHAR(128),
    tipo_de_gestao               VARCHAR(128),

    taxa_de_administracao        NUMERIC(5,2),
    ultimo_rendimento            NUMERIC(19,2),
    cotacao                      NUMERIC(19,2),
    variacao_12m                 NUMERIC(5,2),

    valor_de_mercado             NUMERIC(19,2),
    pvp                          NUMERIC(19,6),
    dividend_yield               NUMERIC(5,2),
    liquidez_diaria              NUMERIC(19,2),
    valor_patrimonial            NUMERIC(19,2),
    valor_patrimonial_por_cota   NUMERIC(19,2),
    vacancia                     NUMERIC(5,2),

    numero_de_cotistas           BIGINT,
    cotas_emitidas               BIGINT,

    dados_brutos_json            JSONB,
    data_atualizacao             TIMESTAMPTZ DEFAULT NOW()
    );

-- Índices
CREATE INDEX IF NOT EXISTS idx_fundo_ticker        ON fundo_imobiliario (ticker);
CREATE INDEX IF NOT EXISTS idx_fundo_internal_id   ON fundo_imobiliario (internal_id);
CREATE INDEX IF NOT EXISTS idx_fundo_atualizacao   ON fundo_imobiliario (data_atualizacao);

-- ================================
-- tabela: fii_dividendo
-- ================================
CREATE TABLE IF NOT EXISTS fii_dividendo (
                                             id                     BIGSERIAL PRIMARY KEY,
                                             mes                    DATE NOT NULL,
                                             valor                  NUMERIC(19,4) NOT NULL,
    fundo_imobiliario_id   BIGINT NOT NULL REFERENCES fundo_imobiliario(id) ON DELETE CASCADE,

    CONSTRAINT uk_fii_dividendo_fundo_mes UNIQUE (fundo_imobiliario_id, mes)
    );

-- Índices auxiliares
CREATE INDEX IF NOT EXISTS idx_fii_dividendo_mes    ON fii_dividendo (mes);
CREATE INDEX IF NOT EXISTS idx_fii_dividendo_fundo  ON fii_dividendo (fundo_imobiliario_id);
