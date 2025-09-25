package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;

public record ClassificacaoResponse(
        String ticker,
        TipoAtivo tipo
    ) {}
