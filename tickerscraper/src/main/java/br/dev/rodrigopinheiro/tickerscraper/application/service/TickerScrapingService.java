package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.AcaoPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.AcaoScraperMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.TickerDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class TickerScrapingService {
    private static final Logger logger = LoggerFactory.getLogger(TickerScrapingService.class);

    private final TickerDataScrapperPort scraper;
    private final AcaoScraperMapper acaoScraperMapper;
    private final AcaoPersistenceMapper acaoPersistenceMapper;
    private final AcaoRepositoryPort acaoRepository;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public TickerScrapingService(@Qualifier("seleniumScraper") TickerDataScrapperPort scraper,
                                 AcaoScraperMapper acaoScraperMapper,
                                 AcaoPersistenceMapper acaoPersistenceMapper,
                                 AcaoRepositoryPort acaoRepository) {
        this.scraper = scraper;
        this.acaoScraperMapper = acaoScraperMapper;
        this.acaoPersistenceMapper = acaoPersistenceMapper;
        this.acaoRepository = acaoRepository;
    }

    /**
     * Orquestra a busca de dados de forma reativa, com lógica de cache.
     *
     * @param ticker O ticker da ação.
     * @return Um Mono contendo a entidade AcaoEntity final.
     */
    public Mono<AcaoEntity> getTickerData(String ticker) {
        // 1. Tenta buscar no banco de forma não-bloqueante
        return Mono.fromCallable(() -> acaoRepository.findByTicker(ticker))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalAcao -> {
                    // 2. Se encontrou, verifica se o cache é válido
                    if (optionalAcao.isPresent()) {
                        AcaoEntity acaoExistente = optionalAcao.get();
                        if (isCacheValid(acaoExistente)) {
                            logger.info("Cache para {} é válido. Retornando do banco de dados.", ticker);
                            return Mono.just(acaoExistente); // Retorna a entidade do cache
                        }
                    }
                    // 3. Se não encontrou ou o cache está velho, dispara o scraping
                    logger.info("Cache para {} inválido ou inexistente. Iniciando scraping.", ticker);
                    try {
                        return doScrapeAndSave(ticker, optionalAcao);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Lógica de scraping e salvamento, encapsulada em um fluxo reativo.
     */
    private Mono<AcaoEntity> doScrapeAndSave(String ticker, Optional<AcaoEntity> acaoExistente) throws IOException {
        // A. O scraper retorna a promessa dos dados brutos
        return scraper.scrape(ticker)
                .flatMap(dadosBrutos -> {
                    // B. O mapeamento e a persistência são tarefas bloqueantes, então as encapsulamos
                    return Mono.fromCallable(() -> {
                        // Mapeia os dados brutos para a entidade
                        Acao acao = acaoScraperMapper.toDomain(dadosBrutos);
                        AcaoEntity entity = acaoPersistenceMapper.toEntity(acao);
                        // Serializa o JSON bruto para auditoria
                        try {
                            String jsonString = jsonMapper.writeValueAsString(dadosBrutos);
                            entity.setDadosBrutosJson(jsonString);
                        } catch (JsonProcessingException e) {
                            logger.warn("Não foi possível serializar o JSON bruto para {}", ticker);
                            // Continua mesmo se a serialização falhar
                        }

                        // Garante que será um UPDATE se a entidade já existia
                        acaoExistente.ifPresent(existente -> entity.setId(existente.getId()));

                        // Salva no banco e retorna a entidade final
                        logger.info("Salvando/Atualizando dados para {} no banco de dados.", ticker);
                        return acaoRepository.save(entity);
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }


    /**
     * Regra de negócio para a validade do cache.
     */
    private boolean isCacheValid(AcaoEntity acao) {
        final Duration maxAge = Duration.ofDays(1);
        return Duration.between(acao.getDataAtualizacao(), LocalDateTime.now()).compareTo(maxAge) < 0;
    }
}


