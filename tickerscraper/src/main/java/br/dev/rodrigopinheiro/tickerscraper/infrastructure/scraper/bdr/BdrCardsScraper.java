package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.InfoCards;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document; // Removido o import do 'Page' e usado apenas Jsoup
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BdrCardsScraper {
    private final ObjectMapper json = new ObjectMapper();
    private static final Pattern RX_MONEY   = Pattern.compile("(?i)R\\$\\s*([0-9.,]+)");
    private static final Pattern RX_PERCENT = Pattern.compile("([-+]?\\d+[.,]?\\d*)\\s*%");

    public InfoCards extract(Document doc) {
        BigDecimal cotacao = null;
        Double variacao12m = null;

        var scripts = doc.select("script[type='application/ld+json']");
        for (var script : scripts) {
            try {
                JsonNode node = json.readTree(script.html());
                if (node.isObject() && "FAQPage".equalsIgnoreCase(node.path("@type").asText())) {
                    for (JsonNode q : node.withArray("mainEntity")) {
                        String a = q.path("acceptedAnswer").path("text").asText("");
                        if (cotacao == null)     cotacao     = parseMoney(a);
                        if (variacao12m == null) variacao12m = parsePercent(a);
                        if (cotacao != null && variacao12m != null) break;
                    }
                }
                if (cotacao != null && variacao12m != null) break;
            } catch (Exception ignored) {}
        }

        if (cotacao == null)     cotacao     = fromVisiblePrice(doc);
        if (variacao12m == null) variacao12m = fromVisiblePercent(doc);

        return new InfoCards(cotacao, variacao12m);
    }


    private BigDecimal fromVisiblePrice(Document doc) {
        if (doc == null) return null;
        try {
            // Usa o texto do corpo do documento
            String text = doc.body().text();
            if (text == null) return null;

            BigDecimal best = null;
            Matcher m = RX_MONEY.matcher(text.replace(".", ""));
            while (m.find()) {
                String num = m.group(1).replace(",", ".");
                try {
                    BigDecimal v = new BigDecimal(num);
                    if (v.compareTo(BigDecimal.ONE) < 0) continue; // descarta microvalores
                    if (best == null || v.compareTo(best) > 0) best = v;
                } catch (NumberFormatException ignored) {}
            }
            return best;
        } catch (Exception e) {
            return null;
        }
    }

    private Double fromVisiblePercent(Document doc) {
        if (doc == null) return null;
        try {
            String text = doc.body().text();
            if (text == null) return null;

            Matcher m = RX_PERCENT.matcher(text);
            if (m.find()) {
                String num = m.group(1).replace(",", ".");
                try { return Double.parseDouble(num); } catch (NumberFormatException ignored) {}
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal parseMoney(String t) {
        if (t == null) return null;
        Matcher m = RX_MONEY.matcher(t.replace(".", ""));
        if (m.find()) {
            String num = m.group(1).replace(".", "").replace(",", ".");
            try { return new BigDecimal(num); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private static Double parsePercent(String t) {
        if (t == null) return null;
        Matcher m = RX_PERCENT.matcher(t);
        if (m.find()) {
            String num = m.group(1).replace(",", ".");
            try { return Double.parseDouble(num); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}