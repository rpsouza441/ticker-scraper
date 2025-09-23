package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.*;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BdrApiMapper {

    @Mappings({
            @Mapping(source = "historicoDePrecos", target = "historicoDePrecos"),
            @Mapping(source = "dividendosPorAno", target = "dividendosPorAno"),
            @Mapping(source = "indicadoresHistoricos", target = "indicadoresHistoricos"),
            @Mapping(source = "dreAnual", target = "dreAnual"),
            @Mapping(source = "bpAnual", target = "balancoPatrimonial"),
            @Mapping(source = "fcAnual", target = "fluxoDeCaixa"),
            @Mapping(source = "updatedAt", target = "atualizadoEm")
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
