package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiCotacaoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiDividendoDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiIndicadorHistoricoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Componente especialista em interagir com as APIs externas de FIIs do Investidor10.
 * A responsabilidade desta classe é fazer as chamadas de rede e desserializar
 * as respostas JSON para os DTOs correspondentes, tratando erros de forma reativa.
 */
@Component
public class FiiApiScraper {
    private static final Logger logger = LoggerFactory.getLogger(FiiApiScraper.class);

    private final WebClient webClient;

    public FiiApiScraper(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Busca de forma assíncrona os dados da API de histórico de indicadores.
     * O WebClient/Jackson lê a resposta JSON e a converte diretamente para o record FiiIndicadorHistoricoDTO.
     * A estrutura do DTO, com um Map aninhado, é projetada para espelhar a resposta da API.
     *
     * @param url A URL completa da API de histórico capturada pelo Selenium.
     * @return Um Mono (uma "promessa") contendo o DTO de histórico preenchido. Em caso de erro na chamada da API,
     * retorna um Mono com um DTO contendo um mapa vazio, garantindo que o fluxo reativo não seja quebrado.
     */
    public Mono<FiiIndicadorHistoricoDTO> fetchHistorico(String url) {
        logger.info("Chamando API de Histórico: {}", url);
        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(FiiIndicadorHistoricoDTO.class)
                .doOnError(e -> logger.error("Falha ao buscar histórico da API: {}", url, e))
                .onErrorReturn(new FiiIndicadorHistoricoDTO(Collections.emptyMap()));
    }

    /**
     * Busca de forma assíncrona a cotação mais recente do FII a partir da API.
     * O WebClient/Jackson converte o objeto JSON de resposta diretamente para o record FiiCotacaoDTO.
     *
     * @param url A URL completa da API de cotação capturada pelo Selenium.
     * @return Um Mono contendo o DTO de cotação preenchido. Em caso de erro, retorna um Mono com um
     * DTO contendo valores nulos para não interromper o fluxo de dados.
     */
    public Mono<FiiCotacaoDTO> fetchCotacao(String url) {
        logger.info("Chamando API de Cotação: {}", url);
        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(FiiCotacaoDTO.class)
                .doOnError(e -> logger.error("Falha ao buscar cotação da API: {}", url, e))
                .onErrorReturn(new FiiCotacaoDTO(null, null));
    }

    /**
     * Busca de forma assíncrona a lista de dividendos pagos pelo FII a partir da API.
     * A resposta da API é um Array JSON, que precisa ser mapeado para uma Lista de DTOs.
     * Para isso, utilizamos o ParameterizedTypeReference para informar ao WebClient sobre o tipo genérico List<FiiDividendoDTO>.
     *
     * @param url A URL completa da API de dividendos capturada pelo Selenium.
     * @return Um Mono contendo a LISTA de DTOs de dividendo. Em caso de erro na chamada da API,
     * retorna um Mono com uma lista vazia, mantendo a integridade do fluxo reativo.
     */
    public Mono<List<FiiDividendoDTO>> fetchDividendos(String url) {
        logger.info("Chamando API de Dividendos: {}", url);
        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FiiDividendoDTO>>() {})
                .doOnError(e -> logger.error("Falha ao buscar dividendos da API: {}", url, e))
                .onErrorReturn(Collections.emptyList());
    }
}