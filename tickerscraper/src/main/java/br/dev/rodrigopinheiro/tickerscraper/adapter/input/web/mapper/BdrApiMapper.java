package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.*;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BdrApiMapper {

    @Mappings({
            @Mapping(source = "nomeBdr", target = "nomeEmpresa"),
            @Mapping(source = "setor", target = "mercado"),
            @Mapping(source = "priceCurrency", target = "moedaDeReferencia"),
            @Mapping(source = "cotacao", target = "precoAtual"),
            @Mapping(source = "variacao12", target = "variacaoAno"),
            @Mapping(source = "priceSeries", target = "historicoDePrecos"),
            @Mapping(source = "dividendYears", target = "dividendosPorAno"),
            @Mapping(source = "historicalIndicators", target = "indicadoresHistoricos"),
            @Mapping(source = "dreYears", target = "dreAnual"),
            @Mapping(source = "bpYears", target = "balancoPatrimonial"),
            @Mapping(source = "fcYears", target = "fluxoDeCaixa"),
            @Mapping(source = "updatedAt", target = "atualizadoEm"),
            @Mapping(target = "nomeAcaoOriginal", ignore = true),
            @Mapping(target = "codigoNegociacao", ignore = true),
            @Mapping(target = "paisDeNegociacao", ignore = true),
            @Mapping(target = "variacaoDia", ignore = true),
            @Mapping(target = "variacaoMes", ignore = true),
            @Mapping(target = "dividendYield", ignore = true),
            @Mapping(target = "precoAlvo", ignore = true)
    })
    BdrResponseDTO toResponse(Bdr domain);

    BdrParidadeResponseDTO toResponse(ParidadeBdr paridade);

    BdrCurrentIndicatorsResponseDTO toResponse(CurrentIndicators indicators);

    BdrPricePointResponseDTO toResponse(PricePoint pricePoint);

    BdrDividendYearResponseDTO toResponse(DividendYear year);

    BdrHistoricalIndicatorResponseDTO toResponse(HistoricalIndicator indicator);

    BdrDreYearResponseDTO toResponse(DreYear year);

    BdrBpYearResponseDTO toResponse(BpYear year);

    BdrFcYearResponseDTO toResponse(FcYear year);
}
