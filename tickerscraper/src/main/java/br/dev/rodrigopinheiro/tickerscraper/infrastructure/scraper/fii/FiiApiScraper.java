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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public Mono<FiiIndicadorHistoricoDTO> fetchHistorico(String url, Map<String, String> headers) {
        logger.info("Chamando API de Histórico: {}", url);
        return prepareRequest(url, headers)
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
    public Mono<FiiCotacaoDTO> fetchCotacao(String url, Map<String, String> headers) {
        logger.info("Chamando API de Cotação: {}", url);
        return prepareRequest(url, headers)
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
    public Mono<List<FiiDividendoDTO>> fetchDividendos(String url, Map<String, String> headers) {
        logger.info("Chamando API de Dividendos: {}", url);
        return prepareRequest(url, headers)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FiiDividendoDTO>>() {})
                .doOnNext(dividendos -> {
                    if (!dividendos.isEmpty()) {
                        logger.debug("Processados {} dividendos da API", dividendos.size());
                    }
                })
                .doOnError(e -> logger.error("Falha ao buscar dividendos da API: {}", url, e))
                .onErrorReturn(Collections.emptyList());
    }

    private WebClient.RequestHeadersSpec<?> prepareRequest(String url, Map<String, String> headers) {
        WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url);

        if (headers == null || headers.isEmpty()) {
            return request;
        }

        Map<String, String> headerCopy = new ConcurrentHashMap<>();
        String cookieHeader = null;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if ("cookie".equalsIgnoreCase(entry.getKey())) {
                cookieHeader = entry.getValue();
            } else {
                headerCopy.put(entry.getKey(), entry.getValue());
            }
        }

        request = request.headers(h -> headerCopy.forEach(h::add));

        if (cookieHeader != null) {
            Arrays.stream(cookieHeader.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(c -> {
                        String[] parts = c.split("=", 2);
                        if (parts.length == 2) {
                            request.cookie(parts[0].trim(), parts[1].trim());
                        }
                    });
        }

        return request;
    }

}