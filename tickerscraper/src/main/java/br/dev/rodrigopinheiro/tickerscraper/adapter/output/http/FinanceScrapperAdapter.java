package br.dev.rodrigopinheiro.tickerscraper.adapter.output;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.FinanceScraperPort;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;

public class FinanceScrapperAdapter implements FinanceScraperPort {

    @Override
    public Acao buscarDadosDeAcao(String ticker) {
        return null;
    }
}
