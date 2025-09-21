package br.dev.rodrigopinheiro.tickerscraper.application.service;


import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.TipoAtivoResult;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivoFinanceiroVariavel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple4;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class TickerDatabaseStrategy {

    private final AcaoRepositoryPort acaoRepository;
    private final FiiRepositoryPort fiiRepository;
    // TODO: Adicionar EtfRepositoryPort e BdrRepositoryPort quando criados
    
    /**
     * Verifica se ticker existe no banco e retorna o tipo
     */
    public Mono<TipoAtivoResult> verificarTickerNoBanco(String ticker) {
        log.debug("Verificando ticker {} no banco de dados", ticker);
        
        // Busca paralela em todas as tabelas
        Mono<Boolean> existeAcao = verificarExistencia(acaoRepository::existsByTicker, ticker);
        Mono<Boolean> existeFii = verificarExistencia(fiiRepository::existsByTicker, ticker);
        Mono<Boolean> existeEtf = Mono.just(false); // TODO: implementar quando EtfRepository existir
        Mono<Boolean> existeBdr = Mono.just(false); // TODO: implementar quando BdrRepository existir
        
        return Mono.zip(existeAcao, existeFii, existeEtf, existeBdr)
            .map(tuple -> determinarTipo(ticker, tuple))
            .doOnNext(result -> log.info("Ticker {} no BD: encontrado={}, tipo={}", 
                ticker, result.isEncontrado(), result.getTipo()));
    }

    /**
     * Verifica existência com tratamento de erro
     */
    private Mono<Boolean> verificarExistencia(Function<String, Boolean> repositoryMethod, String ticker) {
        return Mono.fromCallable(() -> repositoryMethod.apply(ticker))
            .onErrorReturn(false)
            .subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Determina tipo baseado nos resultados das consultas
     */
    private TipoAtivoResult determinarTipo(String ticker, Tuple4<Boolean, Boolean, Boolean, Boolean> tuple) {
        boolean existeAcao = tuple.getT1();
        boolean existeFii = tuple.getT2();
        boolean existeEtf = tuple.getT3();
        boolean existeBdr = tuple.getT4();
        
        // Prioridade: FII > ETF > BDR > Ação
        if (existeFii) {
            return TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.FII);
        }
        
        if (existeEtf) {
            return TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.ETF);
        }
        
        if (existeBdr) {
            // Determinar se é patrocinado ou não baseado no sufixo
            var tipo = TipoAtivoFinanceiroVariavel.classificarPorSufixo(ticker);
            if (tipo.isBdr()) {
                return TipoAtivoResult.encontrado(tipo);
            }
            return TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.BDR_NAO_PATROCINADO);
        }
        
        if (existeAcao) {
            // Determinar tipo específico de ação baseado no sufixo
            var tipo = TipoAtivoFinanceiroVariavel.classificarPorSufixo(ticker);
            if (tipo.isAcao()) {
                return TipoAtivoResult.encontrado(tipo);
            }
            return TipoAtivoResult.encontrado(TipoAtivoFinanceiroVariavel.ACAO_ON);
        }
        
        return TipoAtivoResult.naoEncontrado();
    }
}
