package project.muramidara.testProjectSENLA.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import project.muramidara.testProjectSENLA.dto.SymbolPageDto;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SymbolPageDtoMapper {
    private final SymbolDataMapper symbolDataMapper;

    public SymbolPageDto mapEntityPageToDtoPage(Page<SymbolDataEntity> entityPage){
        return SymbolPageDto.builder()
                .items(entityPage.get().map(symbolDataMapper::mapEntityToDto).collect(Collectors.toList()))
                .page(entityPage.getNumber())
                .size(entityPage.getSize())
                .totalElements(entityPage.getTotalElements())
                .hasNext(entityPage.hasNext())
                .build();
    }

}
