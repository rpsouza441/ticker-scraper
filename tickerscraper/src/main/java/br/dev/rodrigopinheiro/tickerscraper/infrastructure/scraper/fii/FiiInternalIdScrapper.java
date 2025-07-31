package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.fii;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class FiiInternalIdScrapper {
    private static final Logger logger = LoggerFactory.getLogger(FiiInternalIdScrapper.class);

    /**
     * Extrai o ID numérico de uma lista de URLs e valida se ele é único.
     * Esta é uma sub-rotina de scraping, encapsulada para manter o métod principal limpo.
     *
     * @param urls A lista de URLs capturadas do listener de rede.
     * @return O ID interno único como um Integer, ou null se não for encontrado ou se houver ambiguidade.
     */
    public Integer scrape (List<String> urls){

        // Padrão de regex para encontrar um ou mais dígitos que estão entre duas barras.
        // Ex: em ".../fii/dividendos/chart/10/360/mes", ele captura o "10".
        Pattern pattern = Pattern.compile("/(\\d+)/");

        // Usamos um Set para coletar apenas os IDs únicos.
        Set<Integer> idsEncontrados = urls.stream()
                .map(pattern::matcher)
                .filter(Matcher::find)
                .map(matcher -> Integer.parseInt(matcher.group(1)))
                .collect(Collectors.toSet());

        if (idsEncontrados.size() == 1) {
            logger.info("ID interno validado com sucesso: {}", idsEncontrados.iterator().next());
            return idsEncontrados.iterator().next(); // Retorna o único elemento do Set.
        } else if (idsEncontrados.isEmpty()) {
            logger.error("Nenhum ID interno foi encontrado nas URLs capturadas: {}", urls);
            return null;
        } else {
            logger.error("Inconsistência de dados detectada! Múltiplos IDs internos encontrados: {}", idsEncontrados);
            return null;
        }
    }
}
