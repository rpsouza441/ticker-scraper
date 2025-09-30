package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.InfoSobre;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class BdrInfoSobreScraper {

    public InfoSobre extract(Document doc) {
        // CORREÇÃO: Usar os rótulos exatos do HTML da página
        String marketCap = findValue(doc, "Valor de mercado");
        String setor = findValue(doc, "Setor");
        String industria = findValue(doc, "Indústria");
        String paridade = findValue(doc, "Paridade da BDR");

        return new InfoSobre(
                clean(marketCap), clean(setor), clean(industria), clean(paridade)
        );
    }

    /**
     * Lógica de busca robusta com Jsoup.
     * Itera sobre cada 'cell', verifica o texto do 'title' e, se corresponder,
     * extrai o texto do 'value' ou 'simple-value'.
     */
    private String findValue(Document doc, String label) {
        if (doc == null || label == null) return null;

        try {
            // O contêiner principal das informações
            Element container = doc.selectFirst("#table-indicators-company");
            if (container == null) return null;

            for (Element cell : container.select(".cell")) {
                Element titleEl = cell.selectFirst(".title");

                if (titleEl != null && titleEl.text().trim().equalsIgnoreCase(label)) {
                    // Tenta encontrar o valor em '.value' ou '.simple-value' dentro da célula
                    Element valueEl = cell.selectFirst(".value, .simple-value");
                    if (valueEl != null) {
                        return valueEl.text().trim();
                    }
                }
            }
        } catch (Exception e) {
            // Logar o erro pode ser útil para depuração futura
            return null;
        }
        return null; // Retorna nulo se não encontrar
    }

    private String clean(String s) {
        if (s == null) return null;
        return s.replace("\u00A0", " ").trim();
    }
}