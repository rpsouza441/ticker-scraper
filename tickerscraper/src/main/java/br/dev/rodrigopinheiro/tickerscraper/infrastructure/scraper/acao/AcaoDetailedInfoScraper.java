package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.AcaoInfoDetailed;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Component
public class AcaoDetailedInfoScraper {

    public AcaoInfoDetailed scrapeAndParseDetailedInfo(Document doc) {
        Map<String, String> detailsMap = Optional.ofNullable(doc.selectFirst("div#info_about div.content"))
                .map(container -> container.select("div.cell").stream()
                        .map(cell -> {
                            String title = Optional.ofNullable(cell.selectFirst("span.title"))
                                    .map(Element::text).orElse("");
                            String value = Optional.ofNullable(cell.selectFirst("div.detail-value"))
                                    .or(() -> Optional.ofNullable(cell.selectFirst("span.value")))
                                    .map(Element::text)
                                    .orElse("");
                            return new SimpleEntry<>(title, value);
                        })
                        .filter(entry -> !entry.getKey().isEmpty() && !entry.getValue().isEmpty())
                        .collect(toMap(
                                SimpleEntry::getKey,
                                SimpleEntry::getValue,
                                (oldValue, newValue) -> newValue,
                                LinkedHashMap::new
                        ))
                )
                .orElseGet(LinkedHashMap::new);


        final ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(detailsMap, AcaoInfoDetailed.class);
    }
}