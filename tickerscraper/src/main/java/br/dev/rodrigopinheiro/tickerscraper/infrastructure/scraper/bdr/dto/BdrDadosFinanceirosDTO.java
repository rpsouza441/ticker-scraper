package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Agregador com todas as informações capturadas durante o scraping de um BDR.
 */
public record BdrDadosFinanceirosDTO(
        String ticker,
        String investidorId,
        BdrCotacoesDTO cotacoes,
        BdrDividendosDTO dividendos,
        BdrIndicadoresDTO indicadores,
        BdrDemonstrativoDTO dre,
        BdrDemonstrativoDTO balancoPatrimonial,
        BdrDemonstrativoDTO fluxoDeCaixa,
        BdrHtmlMetadataDTO htmlMetadata,
        Map<String, String> rawJson,
        Instant updatedAt
) {
    public BdrDadosFinanceirosDTO {
        cotacoes = cotacoes == null ? BdrCotacoesDTO.empty() : cotacoes;
        dividendos = dividendos == null ? BdrDividendosDTO.empty() : dividendos;
        indicadores = indicadores == null ? BdrIndicadoresDTO.empty() : indicadores;
        dre = dre == null ? BdrDemonstrativoDTO.empty("DRE") : dre;
        balancoPatrimonial = balancoPatrimonial == null ? BdrDemonstrativoDTO.empty("BP") : balancoPatrimonial;
        fluxoDeCaixa = fluxoDeCaixa == null ? BdrDemonstrativoDTO.empty("FC") : fluxoDeCaixa;
        htmlMetadata = htmlMetadata == null ? BdrHtmlMetadataDTO.empty(null) : htmlMetadata;
        rawJson = rawJson == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(rawJson));
    }
}
