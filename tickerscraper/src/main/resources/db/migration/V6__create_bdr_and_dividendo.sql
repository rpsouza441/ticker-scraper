-- V6__create_bdr_and_dividendo.sql
-- PostgreSQL

-- =========================================================
-- Tabela: bdr  (mapeia BdrEntity)
-- =========================================================
CREATE TABLE IF NOT EXISTS bdr (
                                   id                          BIGSERIAL PRIMARY KEY,

    -- Identificação e auditoria
                                   investidor_id               INTEGER,
                                   updated_at                  TIMESTAMPTZ NOT NULL,

    -- Identidade do papel
                                   ticker                      VARCHAR(20) NOT NULL,
    nome_bdr                    VARCHAR(200),
    setor                       VARCHAR(120),
    industria                   VARCHAR(160),

    -- Moedas / cotação
    price_currency              VARCHAR(3)  NOT NULL,
    financials_currency         VARCHAR(3)  NOT NULL,
    cotacao                     NUMERIC(19,6),
    variacao_12                 DOUBLE PRECISION,

    -- Market cap
    market_cap_value            NUMERIC(24,2),
    market_cap_currency         VARCHAR(3),

    -- Paridade
    paridade_ratio              NUMERIC(19,6),
    paridade_last_verified_at   TIMESTAMPTZ,

    -- Indicadores (current)
    pl                          NUMERIC(19,6),
    pvp                         NUMERIC(19,6),
    psr                         NUMERIC(19,6),
    p_ebit                      NUMERIC(19,6),
    p_ebitda                    NUMERIC(19,6),
    p_ativo                     NUMERIC(19,6),
    roe                         NUMERIC(19,6),
    roic                        NUMERIC(19,6),
    roa                         NUMERIC(19,6),
    margem_bruta                NUMERIC(19,6),
    margem_operacional          NUMERIC(19,6),
    margem_liquida              NUMERIC(19,6),
    vpa                         NUMERIC(19,6),
    lpa                         NUMERIC(19,6),

    -- DRE (último ano)
    dre_year                    INTEGER,
    receita_total_usd           NUMERIC(24,2),
    lucro_bruto_usd             NUMERIC(24,2),
    ebitda_usd                  NUMERIC(24,2),
    ebit_usd                    NUMERIC(24,2),
    lucro_liquido_usd           NUMERIC(24,2),

    -- BP (último ano)
    bp_year                     INTEGER,
    ativos_totais_usd           NUMERIC(24,2),
    passivos_totais_usd         NUMERIC(24,2),
    divida_lp_usd               NUMERIC(24,2),
    pl_usd                      NUMERIC(24,2),

    -- FC (último ano)
    fc_year                     INTEGER,
    fco_usd                     NUMERIC(24,2),
    fci_usd                     NUMERIC(24,2),
    fcf_usd                     NUMERIC(24,2),

    -- Dividendos (último ano) - JSON serializado do objeto de domínio
    dividendo_json              TEXT
    );

-- Índices bdr
CREATE INDEX IF NOT EXISTS ix_bdr_ticker      ON bdr (ticker);
CREATE INDEX IF NOT EXISTS ix_bdr_updated_at  ON bdr (updated_at);


-- =========================================================
-- Tabela: dividendo  (mapeia DividendoEntity)
-- PK composta (mes, tipo_dividendo)
-- =========================================================
CREATE TABLE IF NOT EXISTS dividendo (
                                         mes             VARCHAR(7)  NOT NULL,     -- formato 'yyyy-MM'
    tipo_dividendo  VARCHAR(20) NOT NULL,     -- enum como texto
    valor           NUMERIC(18,6) NOT NULL,
    currency        VARCHAR(3)  NOT NULL,

    CONSTRAINT pk_dividendo PRIMARY KEY (mes, tipo_dividendo),

    -- Checks mínimos de sanidade
    CONSTRAINT ck_dividendo_mes_format
    CHECK (mes ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),
    CONSTRAINT ck_dividendo_currency
    CHECK (currency ~ '^[A-Z]{3}$')
    );

-- Índices dividendo
CREATE INDEX IF NOT EXISTS ix_div_mes  ON dividendo (mes);
CREATE INDEX IF NOT EXISTS ix_div_tipo ON dividendo (tipo_dividendo);
