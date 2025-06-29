package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper;


import br.dev.rodrigopinheiro.tickerscraper.domain.model.IndicadorFundamentalista;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;

@Component
public class IndicatorsScraper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Map<String, IndicadorFundamentalista> scrape(Document doc, String ticker){
        return Optional.ofNullable(doc.selectFirst("#table-indicators"))
                .map(grid -> grid.select(".cell").stream()
                        .map(cell -> {
                            String titulo = Optional.ofNullable(cell.selectFirst("span"))
                                    .map(Element::ownText)
                                    .map(String::trim)
                                    .map(t -> t.replace(" - " + ticker.toUpperCase(), "").trim())
                                    .orElse("");
                            Map<String, String> dataMap = new LinkedHashMap<>();
                            Optional.ofNullable(cell.selectFirst("div.value > span"))
                                    .map(Element::text).ifPresent(v-> dataMap.put("valor", v));

                            List<String> definicoes = scrapeIndicatorDescription(cell);
                            if (!definicoes.isEmpty()) {
                                dataMap.put("definicao", definicoes.get(0));
                                if (definicoes.size()>1){
                                    dataMap.put("calculo", definicoes.get(1));
                                }
                            }
                            Optional.ofNullable(cell.selectFirst(".sector .destaque"))
                                    .map(Element::text).ifPresent(v -> dataMap.put("Setor", v));
                            Optional.ofNullable(cell.selectFirst(".subsector .destaque"))
                                    .map(Element::text).ifPresent(v -> dataMap.put("Subsetor", v));
                            Optional.ofNullable(cell.selectFirst(".segment .destaque"))
                                    .map(Element::text).ifPresent(v -> dataMap.put("Segmento", v));

                            return new AbstractMap.SimpleEntry<>(titulo, dataMap);
                        })
                        .filter(entry -> !entry.getKey().isEmpty())
                        .collect(Collectors.toMap(
                                SimpleEntry::getKey,
                                entry -> MAPPER.convertValue(entry.getValue(), IndicadorFundamentalista.class), // O valor é o objeto convertido
                                (oldValue, newValue) -> newValue,
                                LinkedHashMap::new
                        ))
                )
                .orElseGet(LinkedHashMap::new);
    }
    public Map<String, Object> scrapeFundamentalIndicatorsAndDescriptions(Document doc, String ticker) {
        Map<String, Object> indicators = new LinkedHashMap<>();
        Element grid = doc.selectFirst("#table-indicators");
        if (grid != null) {
            Elements cells = grid.select(".cell");
            for (Element cell : cells) {
                Element titleElement = cell.selectFirst("span");
                Element valueElement = cell.selectFirst("div.value > span");

                if (titleElement == null || valueElement == null) continue;

                String titulo = titleElement.ownText().trim();
                if (!titulo.isEmpty()) {
                    titulo = titulo.replace(" - " + ticker.toUpperCase(), "").trim();

                    Map<String, String> indicatorData = new LinkedHashMap<>();
                    indicatorData.put("valor", valueElement.text().trim());

                    // --- INTEGRAÇÃO DA NOVA FUNÇÃO ---
                    List<String> definicao = scrapeIndicatorDescription(cell);
                    if (definicao.size() > 0) {
                        indicatorData.put("definicao", definicao.get(0));
                    }
                    if (definicao.size() > 1) {
                        indicatorData.put("calculo", definicao.get(1));
                    }
                    // ---------------------------------

                    Element setorElem = cell.selectFirst(".sector .destaque");
                    if (setorElem != null) indicatorData.put("Setor", setorElem.text().trim());

                    Element subsetorElem = cell.selectFirst(".subsector .destaque");
                    if (subsetorElem != null) indicatorData.put("Subsetor", subsetorElem.text().trim());

                    Element segmentoElem = cell.selectFirst(".segment .destaque");
                    if (segmentoElem != null) indicatorData.put("Segmento", segmentoElem.text().trim());

                    indicators.put(titulo, indicatorData);
                }
            }
        }
        return indicators;
    }

    /**
     * Extrai a descrição e a fórmula de dentro do atributo 'data-content'.
     */
    private List<String> scrapeIndicatorDescription(Element cell) {
        List<String> descriptionParts = new ArrayList<>();
        Element elementWithData = cell.selectFirst("[data-content]");

        if (elementWithData != null) {
            String dataContentHtml = elementWithData.attr("data-content");
            if (dataContentHtml != null && !dataContentHtml.isEmpty()) {
                Document fragment = Jsoup.parseBodyFragment(dataContentHtml);
                fragment.select("p").forEach(p -> descriptionParts.add(p.text()));
            }
        }
        return descriptionParts;
    }
}
