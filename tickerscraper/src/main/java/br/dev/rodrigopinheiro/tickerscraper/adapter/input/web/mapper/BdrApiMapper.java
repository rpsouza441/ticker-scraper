package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrDividendoResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.BdrResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Dividendo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface BdrApiMapper {

    // Mapeia o objeto de domínio Bdr para o DTO de resposta da API.
    // MapStruct lida com a maioria dos campos automaticamente devido aos nomes iguais.
    BdrResponseDTO toResponse(Bdr domain);

    // Mapeia o objeto de domínio Dividendo para o DTO de resposta, formatando a data.
    @Mappings({
            @Mapping(source = "mes", target = "mes", qualifiedByName = "yearMonthToString"),
            @Mapping(source = "valor", target = "valor"),
            @Mapping(source = "moeda", target = "moeda")
    })
    BdrDividendoResponseDTO toResponse(Dividendo domain);

    @Named("yearMonthToString")
    default String yearMonthToString(YearMonth mes) {
        if (mes == null) {
            return null;
        }
        return mes.format(DateTimeFormatter.ofPattern("MM/yyyy"));
    }
}
