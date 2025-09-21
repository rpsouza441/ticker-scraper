package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivoFinanceiroVariavel;

public record ClassificacaoResponse(
        String ticker,
        TipoAtivoFinanceiroVariavel tipo
    ) {}
