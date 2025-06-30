package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.InfoCards;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CardsScraper {
    public InfoCards scrapeCardsInfo(Document doc) {
        Element container = doc.selectFirst("section#cards-ticker");

        String cotacao = Optional.ofNullable(container)
                .map(c -> c.selectFirst("div._card.cotacao div._card-body span.value"))
                .map(Element::text)
                .orElse("N/A");

        String variacao12M = Optional.ofNullable(container)
                .map(c -> c.selectFirst("div._card.pl div._card-body span"))
                .map(Element::text)
                .orElse("N/A");

        return new InfoCards(
                IndicadorParser.limparTextoIndicador(cotacao),
                IndicadorParser.limparTextoIndicador(variacao12M)
        );
    }
}
