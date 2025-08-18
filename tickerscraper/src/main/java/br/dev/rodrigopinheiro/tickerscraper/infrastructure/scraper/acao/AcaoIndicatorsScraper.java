package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoIndicadorFundamentalistaDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao.dto.AcaoIndicadoresFundamentalistasDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.validator.ScraperValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;

/**
 * Scraper responsável por extrair indicadores fundamentalistas de ações.
 * Utiliza o ScraperValidator para busca robusta de elementos e extração de texto.
 */
@Component
public class AcaoIndicatorsScraper {
    private static final Logger logger = LoggerFactory.getLogger(AcaoIndicatorsScraper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    // Constantes para seletores CSS com fallbacks
    private static final String[] INDICATORS_TABLE_SELECTORS = {"#table-indicators", ".indicators-table", "table.indicators"};
    private static final String[] CELL_SELECTORS = {".cell", ".indicator-cell", ".indicator-item"};
    private static final String[] TITLE_SELECTORS = {"span", ".title", ".indicator-title"};
    private static final String[] VALUE_SELECTORS = {"div.value > span", ".indicator-value", ".value span"};

    /**
     * Extrai os indicadores fundamentalistas da página de uma ação.
     * Utiliza seletores com fallbacks para maior robustez.
     *
     * @param doc O documento HTML da página
     * @param ticker O ticker da ação
     * @return DTO com os indicadores fundamentalistas
     */
    public AcaoIndicadoresFundamentalistasDTO scrape(Document doc, String ticker) {
        if (doc == null) {
            throw new IllegalArgumentException("Documento HTML não pode ser nulo");
        }
        
        logger.debug("Iniciando extração de indicadores para ticker: {}", ticker);
        
        // Busca a tabela de indicadores com fallbacks
        return ScraperValidator.findElementWithFallbacks(doc, INDICATORS_TABLE_SELECTORS)
                .map(grid -> {
                    // Seleciona todas as células de indicadores com fallbacks
                    List<Element> cells = new ArrayList<>();
                    for (String selector : CELL_SELECTORS) {
                        cells = grid.select(selector);
                        if (!cells.isEmpty()) {
                            logger.debug("Encontradas {} células de indicadores usando seletor: {}", cells.size(), selector);
                            break;
                        }
                    }
                    
                    return cells.stream()
                            .map(cell -> {
                                // Extrai o título do indicador com fallbacks
                                String titulo = ScraperValidator.findElementWithFallbacks(cell, TITLE_SELECTORS)
                                        .flatMap(element -> ScraperValidator.extractTextWithValidation(element, "ownText"))
                                        .map(String::trim)
                                        .map(t -> t.replace(" - " + ticker.toUpperCase(), "").trim())
                                        .orElse("");
                                
                                Map<String, String> dataMap = new LinkedHashMap<>();
                                
                                // Extrai o valor do indicador com fallbacks
                                ScraperValidator.findElementWithFallbacks(cell, VALUE_SELECTORS)
                                        .flatMap(element -> ScraperValidator.extractTextWithValidation(element))
                                        .ifPresent(v -> dataMap.put("valor", v));
                                
                                // Extrai descrições e definições
                                List<String> definicoes = scrapeIndicatorDescription(cell);
                                if (!definicoes.isEmpty()) {
                                    dataMap.put("definicao", definicoes.get(0));
                                    if (definicoes.size() > 1) {
                                        dataMap.put("calculo", definicoes.get(1));
                                    }
                                }
                                
                                // Extrai informações de setor, subsetor e segmento com fallbacks
                                 ScraperValidator.extractTextWithDefault(cell, ".sector .destaque", "")
                                         .filter(v -> !v.isEmpty())
                                         .ifPresent(v -> dataMap.put("Setor", v));
                                         
                                 ScraperValidator.extractTextWithDefault(cell, ".subsector .destaque", "")
                                         .filter(v -> !v.isEmpty())
                                         .ifPresent(v -> dataMap.put("Subsetor", v));
                                         
                                 ScraperValidator.extractTextWithDefault(cell, ".segment .destaque", "")
                                         .filter(v -> !v.isEmpty())
                                         .ifPresent(v -> dataMap.put("Segmento", v));
                                
                                return new AbstractMap.SimpleEntry<>(titulo, dataMap);
                            })
                            .filter(entry -> !entry.getKey().isEmpty())
                            .collect(Collectors.toMap(
                                    SimpleEntry::getKey,
                                    entry -> MAPPER.convertValue(entry.getValue(), AcaoIndicadorFundamentalistaDTO.class),
                                    (oldValue, newValue) -> newValue,
                                    LinkedHashMap::new
                            ));
                })
                .map(indicadoresMap -> {
                    logger.debug("Extraídos {} indicadores fundamentalistas", indicadoresMap.size());
                    return new AcaoIndicadoresFundamentalistasDTO(indicadoresMap);
                })
                .orElseGet(() -> {
                    logger.warn("Tabela de indicadores não encontrada para ticker: {}", ticker);
                    return new AcaoIndicadoresFundamentalistasDTO(new LinkedHashMap<>());
                });
    }

    /**
     * Extrai a descrição e a fórmula de dentro do atributo 'data-content'.
     * Utiliza ScraperValidator para extração mais robusta.
     *
     * @param cell O elemento da célula do indicador
     * @return Lista de strings com descrição e fórmula
     */
    private List<String> scrapeIndicatorDescription(Element cell) {
        List<String> descriptionParts = new ArrayList<>();
        
        // Busca elemento com atributo data-content
        ScraperValidator.findElementWithFallbacks(cell, "[data-content]", "[data-tooltip]", "[title]")
                .map(element -> {
                    // Tenta extrair o conteúdo do atributo data-content
                    String dataContentHtml = element.hasAttr("data-content") ? element.attr("data-content") :
                                 element.hasAttr("data-tooltip") ? element.attr("data-tooltip") :
                                 element.attr("title");
                    
                    if (dataContentHtml != null && !dataContentHtml.isEmpty()) {
                        try {
                            Document fragment = Jsoup.parseBodyFragment(dataContentHtml);
                            fragment.select("p").forEach(p -> descriptionParts.add(p.text()));
                            
                            // Se não encontrou parágrafos, tenta extrair o texto diretamente
                            if (descriptionParts.isEmpty()) {
                                String text = fragment.text().trim();
                                if (!text.isEmpty()) {
                                    descriptionParts.add(text);
                                }
                            }
                        } catch (Exception e) {
                            logger.warn("Erro ao processar conteúdo HTML do tooltip: {}", e.getMessage());
                            // Adiciona o texto bruto como fallback
                            descriptionParts.add(dataContentHtml.replaceAll("<[^>]*>", "").trim());
                        }
                    }
                    return element;
                });
        
        return descriptionParts;
    }
}
