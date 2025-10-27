package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.InfoHeader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document; 
import org.springframework.stereotype.Component;

@Component
public class BdrHeaderScraper {
    private final ObjectMapper json = new ObjectMapper();

    public InfoHeader extract(Document doc, String ticker) {
        String nome = fromJsonLdArticle(doc);
        if (nome == null) {
            try {
                nome = doc.selectFirst("h1, h2").text();
            } catch (Exception ignored) {
            }
        }
        if (nome != null) {
            nome = nome.replace('\u00A0', ' ').trim();
            if (nome.isBlank()) nome = null;
        }
        return new InfoHeader(ticker, nome);
    }

    private String fromJsonLdArticle(Document doc) {
        var scripts = doc.select("script[type='application/ld+json']");
        for (var script : scripts) {
            try {
                JsonNode node = json.readTree(script.html());
                if (node.isObject() && "Article".equalsIgnoreCase(node.path("@type").asText())) {
                    String h = text(node, "headline");
                    if (h != null && !h.isBlank()) return h.trim();
                    String n = text(node, "name");
                    if (n != null && !n.isBlank()) return n.trim();
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static String text(JsonNode n, String f) {
        JsonNode c = n.path(f);
        return (c.isMissingNode() || c.isNull()) ? null : c.asText();
    }
}