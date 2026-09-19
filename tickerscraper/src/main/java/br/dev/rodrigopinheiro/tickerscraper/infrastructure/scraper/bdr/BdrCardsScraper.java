package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.InfoCards;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.parser.IndicadorParser;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.HtmlStructureException;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class BdrCardsScraper {
    public InfoCards extract(Document doc) {
        var quote = doc.selectFirst("#cards-ticker .cotacao ._card-body .value");
        var variation = doc.selectFirst("#cards-ticker .pl ._card-body span");
        if (quote == null || variation == null) {
            throw HtmlStructureException.forMissingElement(null, doc.location(), "BDR cotacao/variacao");
        }
        return new InfoCards(IndicadorParser.parseBigdecimal(quote.text()),
                IndicadorParser.parseBigdecimal(variation.text()).doubleValue());
    }
}
