package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AtivoResponseDTO {

    private String ticker;
    private TipoAtivo tipoAtivo;
    private String nomeEmpresa;
    private LocalDateTime dataAtualizacao;
    private ClassificacaoInfo classificacao;

    // Dados específicos (apenas um será preenchido)
    private AcaoResponseDTO dadosAcao;
    private FiiResponseDTO dadosFii;
    private EtfResponseDTO dadosEtf;
    private BdrResponseDTO dadosBdr;

    // Dados financeiros básicos (sempre preenchidos)
    private BigDecimal cotacao;
    private BigDecimal variacao;
    private BigDecimal variacaoPercentual;
    private Long volume;

    // Dados adicionais da Brapi
    private String currency;
    private String logoUrl;
    private BigDecimal marketCap;
    private BigDecimal regularMarketDayHigh;
    private BigDecimal regularMarketDayLow;
    private String regularMarketDayRange;
    private BigDecimal regularMarketPreviousClose;
    private BigDecimal regularMarketOpen;
    private String fiftyTwoWeekRange;
    private BigDecimal fiftyTwoWeekLow;
    private BigDecimal fiftyTwoWeekHigh;
    private BigDecimal priceEarnings;
    private BigDecimal earningsPerShare;

    @Data
    @Builder
    public static class ClassificacaoInfo {
        private String metodo; // HEURISTICA, BANCO_DADOS, API_BRAPI
        private String confianca; // ALTA, MEDIA, BAIXA
        private String observacao;
    }

    /**
     * Factory method para criar resposta de ação
     */
    public static AtivoResponseDTO fromAcao(String ticker, TipoAtivo tipo, AcaoResponseDTO acaoData) {
        return AtivoResponseDTO.builder()
                .ticker(ticker)
                .tipoAtivo(tipo)
                .nomeEmpresa(acaoData.nomeEmpresa())
                .dataAtualizacao(LocalDateTime.now())
                .dadosAcao(acaoData)
                .cotacao(acaoData.precoAtual())
                .variacao(acaoData.variacao12M())
                .classificacao(ClassificacaoInfo.builder()
                        .metodo(determinarMetodoClassificacao(tipo))
                        .confianca("ALTA")
                        .build())
                .build();
    }

    /**
     * Factory method para criar resposta de FII
     */
    public static AtivoResponseDTO fromFii(String ticker, TipoAtivo tipo, FiiResponseDTO fiiData) {
        return AtivoResponseDTO.builder()
                .ticker(ticker)
                .tipoAtivo(tipo)
                .nomeEmpresa(fiiData.nomeEmpresa())
                .dataAtualizacao(fiiData.dataAtualizacao())
                .dadosFii(fiiData)
                .cotacao(fiiData.cotacao())
                .variacao(fiiData.variacao12M())
                .volume(0L) // FII não tem volume na estrutura atual
                .classificacao(ClassificacaoInfo.builder()
                        .metodo("BANCO_DADOS")
                        .confianca("ALTA")
                        .build())
                .build();
    }

    /**
     * Factory method para criar resposta de ETF
     */
    public static AtivoResponseDTO fromEtf(String ticker, TipoAtivo tipo, EtfResponseDTO etfData) {
        return AtivoResponseDTO.builder()
                .ticker(ticker)
                .tipoAtivo(tipo)
                .nomeEmpresa(etfData.nomeEtf())
                .dataAtualizacao(etfData.dataAtualizacao())
                .dadosEtf(etfData)
                .cotacao(etfData.valorAtual())
                .variacao(etfData.variacao12M())
                .classificacao(ClassificacaoInfo.builder()
                        .metodo("BANCO_DADOS")
                        .confianca("ALTA")
                        .build())
                .build();
    }

    /**
     * Factory method para criar resposta de BDR
     */
    public static AtivoResponseDTO fromBdr(String ticker, TipoAtivo tipo, BdrResponseDTO bdrData) {
        return AtivoResponseDTO.builder()
                .ticker(ticker)
                .tipoAtivo(tipo)
                .nomeEmpresa(bdrData.nome())
                .dataAtualizacao(bdrData.dataAtualizacao().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                .dadosBdr(bdrData)
                .cotacao(bdrData.precoAtual())
                .variacao(bdrData.variacao12M() != null ? BigDecimal.valueOf(bdrData.variacao12M()) : null)
                .classificacao(ClassificacaoInfo.builder()
                        .metodo("BANCO_DADOS")
                        .confianca("ALTA")
                        .build())
                .build();
    }

    /**
     * Factory method para criar resposta da API Brapi
     */
    public static AtivoResponseDTO fromBrapi(String ticker, TipoAtivo tipo,
                                             String shortName, String longName,
                                             BigDecimal regularMarketPrice, BigDecimal regularMarketChange,
                                             BigDecimal regularMarketChangePercent, Long regularMarketVolume,
                                             String currency, String logoUrl, BigDecimal marketCap,
                                             BigDecimal regularMarketDayHigh, BigDecimal regularMarketDayLow,
                                             String regularMarketDayRange, BigDecimal regularMarketPreviousClose,
                                             BigDecimal regularMarketOpen, String fiftyTwoWeekRange,
                                             BigDecimal fiftyTwoWeekLow, BigDecimal fiftyTwoWeekHigh,
                                             BigDecimal priceEarnings, BigDecimal earningsPerShare) {
        return AtivoResponseDTO.builder()
                .ticker(ticker)
                .tipoAtivo(tipo)
                .nomeEmpresa(shortName != null && !shortName.trim().isEmpty() ? shortName : longName)
                .dataAtualizacao(LocalDateTime.now())
                .cotacao(regularMarketPrice)
                .variacao(regularMarketChange)
                .variacaoPercentual(regularMarketChangePercent)
                .volume(regularMarketVolume)
                .currency(currency)
                .logoUrl(logoUrl)
                .marketCap(marketCap)
                .regularMarketDayHigh(regularMarketDayHigh)
                .regularMarketDayLow(regularMarketDayLow)
                .regularMarketDayRange(regularMarketDayRange)
                .regularMarketPreviousClose(regularMarketPreviousClose)
                .regularMarketOpen(regularMarketOpen)
                .fiftyTwoWeekRange(fiftyTwoWeekRange)
                .fiftyTwoWeekLow(fiftyTwoWeekLow)
                .fiftyTwoWeekHigh(fiftyTwoWeekHigh)
                .priceEarnings(priceEarnings)
                .earningsPerShare(earningsPerShare)
                .classificacao(ClassificacaoInfo.builder()
                        .metodo("API_BRAPI")
                        .confianca("MEDIA")
                        .observacao("Dados obtidos da API Brapi")
                        .build())
                .build();
    }

    @Deprecated
    private static String determinarMetodoClassificacao(TipoAtivo tipo) {
        // Lógica para determinar como foi classificado
        return switch (tipo) {
            case ACAO_ON, ACAO_PN, ACAO_PNA, ACAO_PNB, ACAO_PNC, ACAO_PND, FII -> "HEURISTICA";
            case DESCONHECIDO -> "API_BRAPI";
            default -> "BANCO_DADOS";
        };
    }
}