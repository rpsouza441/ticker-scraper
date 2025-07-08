package br.dev.rodrigopinheiro.tickerscraper.application.port.output;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.Acao;

public interface FinanceScraperPort {
//TODO remover
    Acao buscarDadosDeAcao(String ticker);
    //FundoImobiliario buscarDadosDeFii(String ticker);
}
