package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EtfResponseDTO(
        String ticker,
        TipoAtivo tipoAtivo,
        String nomeEtf,
        BigDecimal valorAtual,
        BigDecimal capitalizacao,
        BigDecimal variacao12M,
        BigDecimal variacao60M,
        BigDecimal dy,
        LocalDateTime dataAtualizacao
) {}