package br.dev.rodrigopinheiro.tickerscraper.adapter.output.http;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.ClassificadorAtivoPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.BrapiHttpClient;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.http.brapi.dto.BrapiQuoteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Adaptador que implementa classificação de ativos via API Brapi.
 * 
 * Utiliza regras de negócio para determinar o tipo do ativo baseado
 * nas informações retornadas pela API Brapi.
 * 
 * @author Rodrigo Pinheiro
 * @since 1.0
 */
@Component
public class BrapiClassificadorAdapter implements ClassificadorAtivoPort {
    
    private static final Logger log = LoggerFactory.getLogger(BrapiClassificadorAdapter.class);
    
    private final BrapiHttpClient brapiClient;
    
    public BrapiClassificadorAdapter(BrapiHttpClient brapiClient) {
        this.brapiClient = brapiClient;
    }
    
    @Override
    public Mono<TipoAtivo> classificarPorApi(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            log.debug("Ticker nulo ou vazio fornecido");
            return Mono.empty();
        }
        
        return brapiClient.getQuote(ticker)
            .timeout(Duration.ofSeconds(10))
            .flatMap(response -> {
                if (response == null || !response.hasResults()) {
                    log.debug("Nenhum resultado encontrado para ticker {}", ticker);
                    return Mono.empty();
                }
                
                BrapiQuoteResult result = response.getFirstResult();
                if (result == null || !result.isValid()) {
                    log.debug("Resultado inválido para ticker {}", ticker);
                    return Mono.empty();
                }
                
                TipoAtivo tipo = extrairTipoDoResponse(result);
                log.debug("Ticker {} classificado como {} via API", ticker, tipo);
                return Mono.just(tipo);
            })
            .onErrorResume(e -> {
                log.warn("Erro ao classificar ticker {} via API: {}", ticker, e.getMessage());
                return Mono.empty();
            });
    }
    
    @Override
    public Mono<Boolean> isApiDisponivel() {
        // Testa com um ticker conhecido (PETR4)
        return brapiClient.getQuote("PETR4")
            .map(response -> response.hasResults())
            .doOnSuccess(disponivel -> log.debug("API Brapi disponível: {}", disponivel))
            .onErrorReturn(false)
            .timeout(Duration.ofSeconds(3));
    }
    
    /**
     * Extrai o tipo do ativo baseado na resposta da API Brapi.
     * 
     * Regras de classificação:
     * - Se não tem resultados: DESCONHECIDO
     * - Se shortName/longName contém "FII" ou "FUNDO": FII
     * - Se shortName/longName contém "ETF": ETF
     * - Se shortName/longName contém "BDR": BDR
     * - Se shortName/longName contém "UNIT": ACAO_UNIT
     * - Se ticker termina em 11: FII (fallback para casos ambíguos)
     * - Se ticker termina em 3: ACAO_ON (Ação Ordinária)
     * - Se ticker termina em 4: ACAO_PN (Ação Preferencial)
     * - Se ticker termina em 5: ACAO_PNA (Ação Preferencial Classe A)
     * - Se ticker termina em 6: ACAO_PNB (Ação Preferencial Classe B)
     * - Se ticker termina em 7: ACAO_PNC (Ação Preferencial Classe C)
     * - Se ticker termina em 8: ACAO_PND (Ação Preferencial Classe D)
     * - Caso contrário: DESCONHECIDO
     */
    private TipoAtivo extrairTipoDoResponse(BrapiQuoteResult result) {
        if (!result.isValid()) {
            return TipoAtivo.DESCONHECIDO;
        }
        
        String symbol = result.symbol().toUpperCase();
        String displayName = result.getDisplayName().toUpperCase();
        
        // Classificação por nome (prioridade alta)
        if (displayName.contains("FII") || displayName.contains("FUNDO")) {
            log.debug("Classificado como FII pelo nome: {}", displayName);
            return TipoAtivo.FII;
        }
        
        if (displayName.contains("ETF")) {
            log.debug("Classificado como ETF pelo nome: {}", displayName);
            return TipoAtivo.ETF;
        }
        
        if (displayName.contains("BDR")) {
            log.debug("Classificado como BDR pelo nome: {}", displayName);
            return TipoAtivo.BDR;
        }
        
        if (displayName.contains("UNIT")) {
            log.debug("Classificado como ACAO_UNIT pelo nome: {}", displayName);
            return TipoAtivo.UNIT;
        }
        
        // Classificação por padrão do ticker (fallback)
        if (symbol.matches(".*11$")) {
            log.debug("Classificado como FII pelo padrão do ticker: {}", symbol);
            return TipoAtivo.FII;
        }
        
        // Classificação específica por sufixo de ações
        if (symbol.matches(".*3$")) {
            log.debug("Classificado como ACAO_ON pelo padrão do ticker: {}", symbol);
            return TipoAtivo.ACAO_ON;
        }
        
        if (symbol.matches(".*4$")) {
            log.debug("Classificado como ACAO_PN pelo padrão do ticker: {}", symbol);
            return TipoAtivo.ACAO_PN;
        }
        
        if (symbol.matches(".*5$")) {
            log.debug("Classificado como ACAO_PNA pelo padrão do ticker: {}", symbol);
            return TipoAtivo.ACAO_PNA;
        }
        
        if (symbol.matches(".*6$")) {
            log.debug("Classificado como ACAO_PNB pelo padrão do ticker: {}", symbol);
            return TipoAtivo.ACAO_PNB;
        }
        
        if (symbol.matches(".*7$")) {
            log.debug("Classificado como ACAO_PNC pelo padrão do ticker: {}", symbol);
            return TipoAtivo.ACAO_PNC;
        }
        
        if (symbol.matches(".*8$")) {
            log.debug("Classificado como ACAO_PND pelo padrão do ticker: {}", symbol);
            return TipoAtivo.ACAO_PND;
        }
        
        log.debug("Não foi possível classificar ticker {} - retornando DESCONHECIDO", symbol);
        return TipoAtivo.DESCONHECIDO;
    }
}