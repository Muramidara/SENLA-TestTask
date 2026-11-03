package project.muramidara.testProjectSENLA.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import project.muramidara.testProjectSENLA.dto.AveragePriceDto;
import project.muramidara.testProjectSENLA.dto.SymbolDataDto;
import project.muramidara.testProjectSENLA.dto.SymbolPageDto;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;
import project.muramidara.testProjectSENLA.exception.IllegalDateValueException;
import project.muramidara.testProjectSENLA.exception.SymbolDataNotFoundException;
import project.muramidara.testProjectSENLA.mapper.SymbolDataMapper;
import project.muramidara.testProjectSENLA.mapper.SymbolPageDtoMapper;
import project.muramidara.testProjectSENLA.repository.SymbolDataRepository;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SymbolDataService {

    private final SymbolDataRepository symbolDataRepository;
    private final SymbolDataMapper symbolDataMapper;
    private final SymbolPageDtoMapper symbolPageDtoMapper;

    public SymbolDataDto getLastSymbolData(){
        return symbolDataRepository.findLast()
                .map(symbolDataMapper::mapEntityToDto)
                .orElseThrow(
                () -> new SymbolDataNotFoundException("SymbolData not found")
        );
    }

    public AveragePriceDto getAveragePrice(LocalDateTime fromDate, LocalDateTime toDate){
        if(fromDate.isAfter(toDate)){
            throw new IllegalDateValueException("fromDate(%s) can't be after toDate(%s)".formatted(fromDate, toDate))
        }
        return symbolDataRepository.getAveragePrice(fromDate, toDate)
                .map(AveragePriceDto::new)
                .orElseThrow(() -> new SymbolDataNotFoundException("SymbolData not found"));
    }

    public SymbolPageDto getAllSymbolsInPeriod(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageParameters){
        Page<SymbolDataEntity> symbolDataEntityPage = symbolDataRepository.findAllInPeriod(fromDate, toDate, pageParameters);
        return symbolPageDtoMapper.mapEntityPageToDtoPage(symbolDataEntityPage);
    }

    public SymbolDataDto getTrend(LocalDateTime fromDate, LocalDateTime toDate){
        return null;
    }


}
