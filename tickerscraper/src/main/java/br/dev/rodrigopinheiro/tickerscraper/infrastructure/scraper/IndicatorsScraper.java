package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper;


import br.dev.rodrigopinheiro.tickerscraper.domain.model.IndicadorFundamentalista;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.IndicadoresFundamentalistas;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
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

    public IndicadoresFundamentalistas scrape(Document doc, String ticker){
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
                                entry -> MAPPER.convertValue(entry.getValue(), IndicadorFundamentalista.class),
                                (oldValue, newValue) -> newValue,
                                LinkedHashMap::new
                        ))
                )
                .map(indicadoresMap -> new IndicadoresFundamentalistas(indicadoresMap))
                .orElse(new IndicadoresFundamentalistas(new LinkedHashMap<>()));
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
