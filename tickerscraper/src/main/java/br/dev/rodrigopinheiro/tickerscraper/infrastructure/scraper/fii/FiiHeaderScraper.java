package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoHeaderDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FiiHeaderScraper {
    /**
     * Raspa a seção do cabeçalho de uma página de FII.
     * Alvo: <div class="name-ticker">
     * @param doc O documento HTML completo da página.
     * @return Um DTO com o ticker e o nome da empresa.
     */
    public FiiInfoHeaderDTO scrape(Document doc) {
        //  Seleciona o container que tem o H1 e o H2
        Element container = doc.selectFirst("div.name-ticker");

        //  Extrai o texto do H1 (o Ticker) de forma segura com Optional
        String ticker = Optional.ofNullable(container)
                .map(c -> c.selectFirst("h1"))
                .map(Element::text)
                .orElse("N/A");
        // Extrai o texto do H2 (o Nome) de forma segura
        String nomeEmpresa = Optional.ofNullable(container)
                .map(c -> c.selectFirst("h2.name-company"))
                .map(Element::text)
                .orElse("N/A");
        //  Retorna o DTO preenchido
        return new FiiInfoHeaderDTO(ticker, nomeEmpresa);
    }
}
