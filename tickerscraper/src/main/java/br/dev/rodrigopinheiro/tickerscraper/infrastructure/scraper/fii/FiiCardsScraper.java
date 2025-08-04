package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii.dto.FiiInfoCardsDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class FiiCardsScraper {

    public FiiInfoCardsDTO scrape(Document doc) {

        // --- COTAÇÃO ---
        // A cotação já tem uma classe única ('cotacao'), então o seletor é direto.
        String cotacao = Optional.ofNullable(doc.selectFirst("div._card.cotacao span.value"))
                .map(Element::text)
                .orElse(""); // Deixaremos em branco, pois a cotação virá da API

        // --- VARIAÇÃO (12M) - A SOLUÇÃO ROBUSTA ---

        // 1. Encontra o contêiner do card usando a "âncora" do atributo 'title'.
        Element cardVariacaoContainer = doc.selectFirst("div._card:has(span[title='Variação (12M)'])");

        // 2. A partir do contêiner encontrado, busca o valor dentro do _card-body.
        String variacao12M = Optional.ofNullable(cardVariacaoContainer)
                .map(container -> container.selectFirst("div._card-body span"))
                .map(Element::text)
                .orElse("");

        // Como a cotação virá da API de XHR, retornamos null para ela por enquanto.
        return new FiiInfoCardsDTO(cotacao, variacao12M);
    }
}