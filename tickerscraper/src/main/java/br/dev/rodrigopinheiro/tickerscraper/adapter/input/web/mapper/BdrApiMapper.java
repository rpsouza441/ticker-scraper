package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrBpYearResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.AuditedValueResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrCurrentIndicatorsResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrDividendYearResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrDreYearResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrFcValueResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrFcYearResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrHistoricalIndicatorResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrParidadeResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrPricePointResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrQualityMetricResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.Bdr;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.BpYear;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.CurrentIndicators;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.DividendYear;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.DreYear;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.FcYear;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.HistoricalIndicator;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.ParidadeBdr;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.PricePoint;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.QualityValue;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr.AuditedValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BdrApiMapper {

    @Mapping(source = "nomeBdr", target = "nomeEmpresa")
    @Mapping(source = "setor", target = "mercado")
    @Mapping(source = "priceCurrency", target = "moedaDeReferencia")
    @Mapping(source = "cotacao", target = "precoAtual")
    @Mapping(source = "variacao12", target = "variacaoAno")
    @Mapping(source = "priceSeries", target = "historicoDePrecos")
    @Mapping(source = "dividendYears", target = "dividendosPorAno")
    @Mapping(source = "historicalIndicators", target = "indicadoresHistoricos")
    @Mapping(source = "dreYears", target = "dreAnual")
    @Mapping(source = "bpYears", target = "balancoPatrimonial")
    @Mapping(source = "fcYears", target = "fluxoDeCaixa")
    @Mapping(source = "updatedAt", target = "atualizadoEm")
    @Mapping(target = "nomeAcaoOriginal", ignore = true)
    @Mapping(target = "codigoNegociacao", ignore = true)
    @Mapping(target = "paisDeNegociacao", ignore = true)
    @Mapping(target = "variacaoDia", ignore = true)
    @Mapping(target = "variacaoMes", ignore = true)
    @Mapping(target = "dividendYield", ignore = true)
    @Mapping(target = "precoAlvo", ignore = true)
    BdrResponseDTO toResponse(Bdr domain);

    BdrParidadeResponseDTO toParidadeResponse(ParidadeBdr paridade);

    BdrCurrentIndicatorsResponseDTO toCurrentIndicatorsResponse(CurrentIndicators indicators);

    @Mapping(source = "date", target = "dt")
    @Mapping(source = "close", target = "close")
    BdrPricePointResponseDTO toPricePointResponse(PricePoint pricePoint);


    BdrDividendYearResponseDTO toDividendYearResponse(DividendYear year);

    BdrHistoricalIndicatorResponseDTO toHistoricalIndicatorResponse(HistoricalIndicator indicator);

    BdrDreYearResponseDTO toDreYearResponse(DreYear year);

    BdrBpYearResponseDTO toBpYearResponse(BpYear year);

    BdrFcYearResponseDTO toFcYearResponse(FcYear year);

    default BdrQualityMetricResponseDTO toQualityMetricResponse(DreYear.Metric metric) {
        if (metric == null) {
            return null;
        }
        return new BdrQualityMetricResponseDTO(metric.getValue(), metric.getQuality(), metric.getRaw());
    }

    default AuditedValueResponseDTO toAuditedValueResponse(AuditedValue value) {
        if (value == null) {
            return null;
        }
        return new AuditedValueResponseDTO(value.getValue(), value.getQuality(), value.getRaw());
    }

    default BdrFcValueResponseDTO toFcValueResponse(QualityValue value) {
        if (value == null) {
            return null;
        }
        return new BdrFcValueResponseDTO(value.getValue(), value.getQuality(), value.getRaw());
    }

}
