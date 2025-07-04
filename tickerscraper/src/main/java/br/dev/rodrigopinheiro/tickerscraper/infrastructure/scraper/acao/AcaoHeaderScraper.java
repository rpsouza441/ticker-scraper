package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.acao;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.AcaoInfoHeader;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AcaoHeaderScraper {
    public AcaoInfoHeader scrapeInfoHeader(Document doc) {
        Element container = doc.selectFirst("div.name-ticker");

        String ticker = Optional.ofNullable(container)
                .map(c -> c.selectFirst("h1"))
                .map(Element::text)
                .orElse("N/A");

        String nomeEmpresa = Optional.ofNullable(container)
                .map(c -> c.selectFirst("h2.name-company"))
                .map(Element::text)
                .orElse("N/A");
        return new AcaoInfoHeader(ticker, nomeEmpresa);
    }
}
