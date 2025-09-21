package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.EtfResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Etf;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface EtfApiMapper {

    @Mappings({
            @Mapping(source = "ticker", target = "ticker"),
            @Mapping(source = "tipoAtivo", target = "tipoAtivo"),
            @Mapping(source = "nomeEtf", target = "nomeEtf"),
            @Mapping(source = "valorAtual", target = "valorAtual"),
            @Mapping(source = "capitalizacao", target = "capitalizacao"),
            @Mapping(source = "variacao12M", target = "variacao12M"),
            @Mapping(source = "variacao60M", target = "variacao60M"),
            @Mapping(source = "dy", target = "dy"),
            @Mapping(source = "dataAtualizacao", target = "dataAtualizacao")
    })
    EtfResponseDTO toResponseDto(Etf etf);
}