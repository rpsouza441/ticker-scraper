package br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper;

import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.dto.AcaoResponseDTO;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.AcaoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AcaoApiMapper {

    AcaoResponseDTO toResponseDto(AcaoEntity entity);
}
