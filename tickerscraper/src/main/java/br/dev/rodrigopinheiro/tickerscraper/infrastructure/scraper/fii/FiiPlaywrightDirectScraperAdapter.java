package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FiiDataScrapperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.text.Normalizer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.FiiApiConstants.*;
import static java.util.stream.Collectors.toMap;


@Component("fiiPlaywrightDirectScraper")
public class FiiPlaywrightDirectScraperAdapter implements FiiDataScrapperPort {

    private static final Logger logger = LoggerFactory.getLogger(FiiPlaywrightDirectScraperAdapter.class);

    // Dependências que ainda são necessárias
    private final FiiApiScraper fiiApiScraper;
    private final FiiInternalIdScrapper fiiInternalIdScrapper;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public FiiPlaywrightDirectScraperAdapter(FiiApiScraper fiiApiScraper, FiiInternalIdScrapper fiiInternalIdScrapper) {
        this.fiiApiScraper = fiiApiScraper;
        this.fiiInternalIdScrapper = fiiInternalIdScrapper;
    }

    // Helper record para passar os múltiplos resultados do bloco Playwright para a cadeia reativa
    private record ScrapeResult(
            FiiInfoHeaderDTO infoHeader,
            FiiInfoSobreDTO infoSobre,
            FiiInfoCardsDTO infoCards,
            Map<String, String> urlsMapeadas
    ) {
    }

    @Override
    public Mono<FiiDadosFinanceirosDTO> scrape(String ticker) {
        String urlCompleta = "https://investidor10.com.br/fiis/" + ticker;
        logger.info("Iniciando scraping DIRETO com Playwright para a url {}", urlCompleta);

        return Mono.fromCallable(() -> {
                    try (Playwright playwright = Playwright.create()) {
                        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
                        Page page = browser.newPage();

                        final Map<String, String> urlsMapeadas = new ConcurrentHashMap<>();
                        page.onRequest(request -> {
                            String url = request.url();
                            for (String chave : TODAS_AS_CHAVES) {
                                if (url.contains(chave)) {
                                    urlsMapeadas.putIfAbsent(chave, url);
                                    break;
                                }
                            }
                        });

                        page.navigate(urlCompleta, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                        // Extração direta dos dados usando os métodos privados abaixo
                        FiiInfoHeaderDTO infoHeader = scrapeHeader(page);
                        FiiInfoSobreDTO infoSobre = scrapeInfoSobre(page);
                        FiiInfoCardsDTO infoCards = scrapeCards(page);

                        return new ScrapeResult(infoHeader, infoSobre, infoCards, urlsMapeadas);
                    }
                })
                .flatMap(result -> {
                    // A lógica para extrair o ID e chamar as APIs continua a mesma
                    Integer internalId = fiiInternalIdScrapper.scrape(new ArrayList<>(result.urlsMapeadas().values()));

                    Mono<FiiCotacaoDTO> cotacaoMono = result.urlsMapeadas().containsKey(COTACAO)
                            ? fiiApiScraper.fetchCotacao(result.urlsMapeadas().get(COTACAO))
                            : Mono.just(new FiiCotacaoDTO(null, null));

                    Mono<List<FiiDividendoDTO>> dividendosMono = result.urlsMapeadas().containsKey(DIVIDENDOS)
                            ? fiiApiScraper.fetchDividendos(result.urlsMapeadas().get(DIVIDENDOS))
                            : Mono.just(Collections.emptyList());

                    Mono<FiiIndicadorHistoricoDTO> historicoMono = result.urlsMapeadas().containsKey(HISTORICO_INDICADORES)
                            ? fiiApiScraper.fetchHistorico(result.urlsMapeadas().get(HISTORICO_INDICADORES))
                            : Mono.just(new FiiIndicadorHistoricoDTO(Collections.emptyMap()));

                    return Mono.zip(cotacaoMono, dividendosMono, historicoMono)
                            .map(tuple -> new FiiDadosFinanceirosDTO(
                                    internalId,
                                    result.infoHeader(),
                                    tuple.getT3(),
                                    result.infoSobre(),
                                    result.infoCards(),
                                    tuple.getT2(),
                                    tuple.getT1()
                            ));
                })
                .doOnError(error -> logger.error("O Mono do FiiPlaywrightDirectScraperAdapter falhou para o ticker {}", ticker, error))
                .subscribeOn(Schedulers.boundedElastic());
    }


    // ============================================================================================
    // MÉTODOS DE SCRAPING PRIVADOS (LÓGICA DOS ANTIGOS SCRAPERS CONVERTIDA PARA PLAYWRIGHT)
    // ============================================================================================

    /**
     * Raspa a seção do cabeçalho.
     * Lógica convertida de FiiHeaderScraper.
     */
    private FiiInfoHeaderDTO scrapeHeader(Page page) {
        // ANTES (Jsoup): doc.selectFirst("div.name-ticker").selectFirst("h1").text()
        // AGORA (Playwright): Usa um seletor direto e pega o conteúdo do texto.
        String ticker = page.locator("div.name-ticker h1").textContent();

        // ANTES (Jsoup): doc.selectFirst("div.name-ticker").selectFirst("h2.name-company").text()
        // AGORA (Playwright): Mesmo padrão para o nome da empresa.
        String nomeEmpresa = page.locator("div.name-ticker h2.name-company").textContent();

        return new FiiInfoHeaderDTO(ticker, nomeEmpresa);
    }

    /**
     * Raspa os cards de indicadores principais.
     * Lógica convertida de FiiCardsScraper.
     */
    private FiiInfoCardsDTO scrapeCards(Page page) {
        // ANTES (Jsoup): doc.selectFirst("div._card.cotacao span.value").text()
        // AGORA (Playwright): Localiza o card de cotação e pega o valor.
        String cotacao = page.locator("div._card.cotacao span.value").textContent();

        // ANTES (Jsoup): doc.selectFirst("div._card:has(span[title='Variação (12M)'])").selectFirst("div._card-body span").text()
        // AGORA (Playwright): Usa o seletor :has() para encontrar o card pelo título e depois busca o valor dentro dele.
        // Este padrão é extremamente robusto.
        String variacao12M = page.locator("div._card:has(span[title='Variação (12M)']) div._card-body span").textContent();

        // O mesmo padrão pode ser usado para outros cards, se necessário.
        // Exemplo: String dy = page.locator("div._card:has-text('Dividend Yield') .value").textContent();

        return new FiiInfoCardsDTO(cotacao, variacao12M);
    }

    /**
     * Raspa a tabela de informações "Sobre o FII".
     * Lógica convertida de FiiInfoSobreScraper.
     */
    private FiiInfoSobreDTO scrapeInfoSobre(Page page) {
        // ANTES (Jsoup): doc.selectFirst("div#about-company div.content").select("div.cell").stream()...
        // AGORA (Playwright): Usa locator.all() para pegar todos os elementos que correspondem e então itera sobre eles.
        List<Locator> cells = page.locator("div#about-company div.content div.cell").all();

        Map<String, String> infoMap = cells.stream()
                .map(cell -> {
                    // ANTES (Jsoup): cell.selectFirst("span.name").text()
                    // AGORA (Playwright): A partir do locator da célula, busca o locator do título.
                    String tituloBruto = cell.locator("span.name").textContent().trim();

                    // ANTES (Jsoup): cell.selectFirst("div.value span").text()
                    // AGORA (Playwright): Busca o locator do valor.
                    String valor = cell.locator("div.value span").textContent().trim();

                    // A normalização da chave continua sendo uma boa prática.
                    String tituloNormalizado = normalizar(tituloBruto);

                    return new AbstractMap.SimpleEntry<>(tituloNormalizado, valor);
                })
                .filter(entry -> !entry.getKey().isEmpty() && !entry.getValue().isEmpty())
                .collect(toMap(
                        AbstractMap.SimpleEntry::getKey,
                        AbstractMap.SimpleEntry::getValue,
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new
                ));

        // Usa o ObjectMapper para converter o Map para o DTO final, assim como na versão original.
        return objectMapper.convertValue(infoMap, FiiInfoSobreDTO.class);
    }

    /**
     * Método auxiliar para normalizar as chaves dos indicadores.
     * Lógica copiada de IndicadorParser para manter o exemplo autocontido.
     */
    private static String normalizar(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        // Remove acentos
        String nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String semAcentos = pattern.matcher(nfdNormalizedString).replaceAll("");

        // Converte para minúsculas, remove caracteres especiais (exceto números) e substitui espaços por underscore.
        return semAcentos.toLowerCase()
                .replaceAll("\\(.*?\\)", "") // remove parenteses e seu conteudo
                .replaceAll("[^a-z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "_");
    }
}