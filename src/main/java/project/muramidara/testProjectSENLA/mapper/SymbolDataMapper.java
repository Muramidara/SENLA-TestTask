package project.muramidara.testProjectSENLA.mapper;

import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import project.muramidara.testProjectSENLA.dto.SymbolDataDto;
import project.muramidara.testProjectSENLA.dto.SymbolPageDto;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SymbolDataMapper {

    public SymbolDataDto mapEntityToDto(SymbolDataEntity entity){
        return SymbolDataDto.builder().
                symbol(entity.getSymbol())
                .price(entity.getPrice().toPlainString())
                .fetchedAt(entity.getFetchedAt().toString())
                .build();
    }



}
