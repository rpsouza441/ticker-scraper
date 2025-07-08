package br.dev.rodrigopinheiro.tickerscraper.application.service;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.AcaoPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.application.port.input.AcaoUseCasePort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoRepositoryPort;
import br.dev.rodrigopinheiro.tickerscraper.application.port.output.AcaoDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoDadosFinanceiros;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.mapper.AcaoScraperMapper;
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
public class AcaoUseCaseService implements AcaoUseCasePort {
    private static final Logger logger = LoggerFactory.getLogger(AcaoUseCaseService.class);

    private final AcaoDataScrapperPort scraper;
    private final AcaoScraperMapper acaoScraperMapper;
    private final AcaoPersistenceMapper acaoPersistenceMapper;
    private final AcaoRepositoryPort acaoRepository;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public AcaoUseCaseService(@Qualifier("acaoSeleniumScraper") AcaoDataScrapperPort scraper,
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
        final String tickerNormalizado = ticker.toUpperCase().trim();
        // 1. Tenta buscar no banco de forma não-bloqueante
        return Mono.fromCallable(() -> acaoRepository.findByTicker(tickerNormalizado))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalAcao -> {
                    // 2. Se encontrou, verifica se o cache é válido
                    if (optionalAcao.isPresent()) {
                        AcaoEntity acaoExistente = optionalAcao.get();
                        if (isCacheValid(acaoExistente)) {
                            logger.info("Cache para {} é válido. Retornando do banco de dados.", ticker);
                            logger.info("Acao Existente toString {}",acaoExistente.toString());
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
     * Busca os dados brutos e completos de uma ação, usando o cache se possível.
     * Esta é uma operação de "leitura". Ela não salva/atualiza os dados no banco.
     *
     * @param ticker O ticker da ação.
     * @return Um Mono contendo o objeto DadosFinanceiros completo.
     */
    public Mono<AcaoDadosFinanceiros> getRawTickerData(String ticker) {
        final String tickerNormalizado = ticker.toUpperCase().trim();

        logger.info("Buscando dados brutos para {}", tickerNormalizado);

        // 1. Tenta buscar no banco de forma não-bloqueante
        return Mono.fromCallable(() -> acaoRepository.findByTicker(tickerNormalizado))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalAcao -> {
                    // 2. Se encontrou E o cache é válido...
                    if (optionalAcao.isPresent() && isCacheValid(optionalAcao.get())) {
                        logger.info("Cache de DADOS BRUTOS para {} é válido. Deserializando JSON do banco.", tickerNormalizado);

                        // 3. ...nós deserializamos o JSON guardado de volta para o objeto DadosFinanceiros.
                        // Como a deserialização pode ser uma operação de I/O (mesmo que pequena),
                        // a envolvemos em um fromCallable para segurança.
                        return Mono.fromCallable(() ->
                                jsonMapper.readValue(optionalAcao.get().getDadosBrutosJson(), AcaoDadosFinanceiros.class)
                        ).subscribeOn(Schedulers.boundedElastic());
                    }

                    // 4. Se não encontrou ou o cache está velho, simplesmente dispara o scraping
                    logger.info("Cache de DADOS BRUTOS para {} inválido ou inexistente. Fazendo scraping ao vivo.", tickerNormalizado);
                    try {
                        return scraper.scrape(tickerNormalizado);
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


