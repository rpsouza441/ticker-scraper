package br.dev.rodrigopinheiro.tickerscraper.application.service;


import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.AtivoResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper.AcaoApiMapper;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper.EtfApiMapper;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper.FiiApiMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.AcaoUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.EtfUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.FiiUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.TickerUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.TickerClassificationException;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.TickerNotFoundException;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivoFinanceiroVariavel;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.BrapiHttpClient;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TickerUseCaseService implements TickerUseCasePort {
    
    private final TickerDatabaseStrategy databaseStrategy;
    private final BrapiResponseClassifier brapiClassifier;
    private final TickerClassificationCacheService classificationCache;
    private final BrapiHttpClient brapiClient;
    
    // UseCases existentes
    private final AcaoUseCasePort acaoUseCase;
    private final FiiUseCasePort fiiUseCase;
    private final EtfUseCasePort etfUseCase;
    private final AcaoApiMapper acaoMapper;
    private final FiiApiMapper fiiMapper;
    private final EtfApiMapper etfMapper;

    // TODO: Adicionar BdrUseCasePort quando criado


     public Mono<AtivoResponseDTO> obterAtivo(String ticker) {
        log.info("Iniciando busca para ticker: {}", ticker);
        
        return classificarTicker(ticker)
            .flatMap(tipo -> delegarParaUseCaseEspecifico(ticker, tipo))
            .doOnSuccess(response -> log.info("Busca concluída para ticker {}: tipo={}", 
                ticker, response.getTipoAtivo()))
            .doOnError(error -> log.error("Erro ao buscar ticker {}: {}", ticker, error.getMessage()));
    }
    
     @Override
    public Mono<TipoAtivoFinanceiroVariavel> classificarTicker(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            return Mono.error(new TickerClassificationException(ticker, "Ticker não pode ser vazio"));
        }
        
        String tickerNormalizado = ticker.trim().toUpperCase();
        
        // 1. Verificar cache primeiro
        var tipoCache = classificationCache.get(tickerNormalizado);
        if (tipoCache.isPresent()) {
            log.debug("Classificação encontrada no cache: {} -> {}", tickerNormalizado, tipoCache.get());
            return Mono.just(tipoCache.get());
        }
        
        // 2. Consultar banco de dados
        return databaseStrategy.verificarTickerNoBanco(tickerNormalizado)
            .flatMap(resultado -> {
                if (resultado.isEncontrado()) {
                    classificationCache.put(tickerNormalizado, resultado.getTipo());
                    log.info("Ticker {} classificado por dados do banco (evitou API Brapi): {}", 
                        tickerNormalizado, resultado.getTipo());
                    return Mono.just(resultado.getTipo());
                }
                
                // 3. Consultar API Brapi apenas se não há dados no banco
                log.debug("Nenhum dado encontrado no banco para {}, consultando API Brapi", tickerNormalizado);
                return consultarApiEClassificar(tickerNormalizado);
            })
            .onErrorResume(error -> {
                log.error("Erro na classificação de {}: {}", tickerNormalizado, error.getMessage());
                return Mono.error(new TickerClassificationException(tickerNormalizado, error));
            });
    }

 /**
     * Consulta API Brapi, classifica resposta e faz scraping para salvar no banco
     */
    private Mono<TipoAtivoFinanceiroVariavel> consultarApiEClassificar(String ticker) {
        log.debug("Consultando API Brapi para classificar ticker: {}", ticker);
        
        return brapiClient.getQuote(ticker)
            .map(brapiClassifier::classificarPorResposta)
            .doOnNext(tipo -> {
                classificationCache.put(ticker, tipo);
                log.info("Ticker {} classificado via API Brapi: {}", ticker, tipo);
            })
            .flatMap(tipo -> {
                // Após classificar, fazer scraping para salvar os dados no banco
                log.debug("Fazendo scraping de {} para salvar no banco após classificação", ticker);
                return fazerScrapingERetornarTipo(ticker, tipo);
            })
            .onErrorMap(TickerNotFoundException.class, ex -> {
                log.warn("Ticker {} não encontrado na API Brapi: {}", ticker, ex.getMessage());
                return new TickerNotFoundException(ticker, "API_BRAPI", ticker, 
                    List.of("Verifique se o ticker está correto"));
            });
    }
    
    /**
     * Faz scraping dos dados e salva no banco, retornando o tipo
     */
    private Mono<TipoAtivoFinanceiroVariavel> fazerScrapingERetornarTipo(String ticker, TipoAtivoFinanceiroVariavel tipo) {
        return switch (tipo) {
            case ACAO_ON, ACAO_PN, ACAO_PNA, ACAO_PNB, ACAO_PNC, ACAO_PND, UNIT,
                 DIREITO_SUBSCRICAO_ON, DIREITO_SUBSCRICAO_PN,
                 RECIBO_SUBSCRICAO_ON, RECIBO_SUBSCRICAO_PN -> 
                acaoUseCase.getTickerData(ticker)
                    .doOnNext(acaoData -> log.info("Dados de ação {} salvos no banco após classificação", ticker))
                    .map(acaoData -> tipo);
                
            case FII -> 
                fiiUseCase.getTickerData(ticker)
                    .doOnNext(fiiData -> log.info("Dados de FII {} salvos no banco após classificação", ticker))
                    .map(fiiData -> tipo);
                
            case ETF -> 
                etfUseCase.getTickerData(ticker)
                    .doOnNext(etfData -> log.info("Dados de ETF {} salvos no banco após classificação", ticker))
                    .map(etfData -> tipo);
                
            // Para tipos não implementados, apenas retorna o tipo sem scraping
            case ETF_BDR, BDR_NAO_PATROCINADO, BDR_PATROCINADO -> {
                log.warn("Scraping não implementado para tipo {}, apenas classificação salva", tipo);
                yield Mono.just(tipo);
            }
            
            default -> {
                log.warn("Tipo desconhecido {}, retornando sem scraping", tipo);
                yield Mono.just(tipo);
            }
        };
    }
    
    /**
     * Delega para UseCase específico baseado no tipo
     */
    private Mono<AtivoResponseDTO> delegarParaUseCaseEspecifico(String ticker, TipoAtivoFinanceiroVariavel tipo) {
        log.debug("Delegando ticker {} para UseCase do tipo: {}", ticker, tipo);
        
        return switch (tipo) {
            case ACAO_ON, ACAO_PN, ACAO_PNA, ACAO_PNB, ACAO_PNC, ACAO_PND, UNIT,
                 DIREITO_SUBSCRICAO_ON, DIREITO_SUBSCRICAO_PN,
                 RECIBO_SUBSCRICAO_ON, RECIBO_SUBSCRICAO_PN -> 
                acaoUseCase.getTickerData(ticker)
                    .map(acaoData -> AtivoResponseDTO.fromAcao(ticker, tipo, acaoMapper.toResponseDto(acaoData)));
                
            case FII -> 
                fiiUseCase.getTickerData(ticker)
                    .map(fiiData -> AtivoResponseDTO.fromFii(ticker, tipo, fiiMapper.toResponse(fiiData)));
                
            case ETF -> 
                etfUseCase.getTickerData(ticker)
                    .map(etfData -> AtivoResponseDTO.fromEtf(ticker, tipo, etfMapper.toResponseDto(etfData)));
                
            case ETF_BDR -> 
                Mono.error(new UnsupportedOperationException("ETF BDR UseCase ainda não implementado"));
                
            case BDR_NAO_PATROCINADO, BDR_PATROCINADO -> 
                Mono.error(new UnsupportedOperationException("BDR UseCase ainda não implementado"));
            
            default -> 
                Mono.error(new TickerNotFoundException(
                    ticker, 
                    "CLASSIFICATION_SERVICE", 
                    ticker, 
                    List.of("Verifique se o ticker está correto ou se o tipo de ativo é suportado")
                ));
        };
    }
    
}
