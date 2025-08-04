package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoSobreDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Component
public class FiiInfoSobreScraper {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private IndicadorParser indicadorParser;



    public FiiInfoSobreDTO scrape(Document doc) {


        // 1. Encontra o contêiner principal da seção "Informações Sobre"
        Map<String, String> infoMap = Optional.ofNullable(doc.selectFirst("div#about-company div.content"))
                .map(container -> container.select("div.cell").stream() // 2. Pega todas as "células"
                        .map(cell -> {
                            // 3. Extrai o TÍTULO de dentro da célula
                            String tituloBruto = Optional.ofNullable(cell.selectFirst("span.name"))
                                    .map(Element::text)
                                    .map(String::trim)
                                    .orElse("");

                            // 4.  Extrai o VALOR do local correto no HTML do FII
                            String valor = Optional.ofNullable(cell.selectFirst("div.value span"))
                                    .map(Element::text)
                                    .map(String::trim)
                                    .orElse("");

                            // USA O NORMALIZADOR PARA LIMPAR A CHAVE!
                            String titulo = IndicadorParser.normalizar(tituloBruto);


                            return new AbstractMap.SimpleEntry<>(titulo, valor);
                        })
                        .filter(entry -> !entry.getKey().isEmpty() && !entry.getValue().isEmpty())
                        .collect(toMap(
                                AbstractMap.SimpleEntry::getKey,
                                AbstractMap.SimpleEntry::getValue,
                                (oldValue, newValue) -> newValue,
                                LinkedHashMap::new
                        ))
                )
                .orElseGet(LinkedHashMap::new);

        return objectMapper.convertValue(infoMap, FiiInfoSobreDTO.class);
    }
}
