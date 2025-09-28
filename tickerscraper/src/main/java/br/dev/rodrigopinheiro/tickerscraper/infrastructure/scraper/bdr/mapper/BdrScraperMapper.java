package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.mapper;

import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoAtivo;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.bdr.dto.BdrDadosFinanceirosDTO;
import org.mapstruct.Mapper;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface BdrScraperMapper {

    default Bdr toDomain(BdrDadosFinanceirosDTO src) {
        Bdr d = new Bdr();
        String ticker = (src != null && src.infoHeader() != null && src.infoHeader().ticker() != null)
                ? src.infoHeader().ticker().trim().toUpperCase()
                : "UNKNOWN";
        d.setTicker(ticker);
        d.setTipoAtivo(TipoAtivo.BDR);
        d.setDataAtualizacao(Instant.now());
        // preencha gradualmente: precoAtual, priceCurrency, indicators, etc.
        return d;
    }
}
