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
import project.muramidara.testProjectSENLA.dto.TrendDto;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;
import project.muramidara.testProjectSENLA.exception.IllegalDateValueException;
import project.muramidara.testProjectSENLA.exception.SymbolDataNotFoundException;
import project.muramidara.testProjectSENLA.mapper.SymbolDataMapper;
import project.muramidara.testProjectSENLA.mapper.SymbolPageDtoMapper;
import project.muramidara.testProjectSENLA.repository.SymbolDataRepository;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SymbolDataService {

    private final SymbolDataRepository symbolDataRepository;
    private final SymbolDataMapper symbolDataMapper;
    private final SymbolPageDtoMapper symbolPageDtoMapper;

    public SymbolDataDto getLastSymbolData() {
        return symbolDataRepository.findFirstByOrderByFetchedAtDesc()
                .map(symbolDataMapper::mapEntityToDto)
                .orElseThrow(
                        () -> new SymbolDataNotFoundException("SymbolData not found")
                );
    }

    public AveragePriceDto getAveragePrice(LocalDate fromDate, LocalDate toDate) {
        validateDates(fromDate, toDate);
        Instant startInstant = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endInstant = toDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        return symbolDataRepository.getAveragePrice(startInstant, endInstant)
                .map(AveragePriceDto::new)
                .orElseThrow(() -> new SymbolDataNotFoundException("SymbolData not found"));
    }


    public SymbolPageDto getAllSymbolsInPeriod(LocalDate fromDate, LocalDate toDate, Pageable pageParameters) {
        validateDates(fromDate, toDate);
        Instant startInstant = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endInstant = toDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        Page<SymbolDataEntity> symbolDataEntityPage = symbolDataRepository.findAllInPeriod(startInstant, endInstant,
                pageParameters);
        return symbolPageDtoMapper.mapEntityPageToDtoPage(symbolDataEntityPage);
    }

    public TrendDto getTrend(LocalDate fromDate, LocalDate toDate) {
        validateDates(fromDate, toDate);
        Instant startInstant = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endInstant = toDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        BigDecimal averagePrice = symbolDataRepository.getAveragePrice(startInstant, endInstant)
                .orElseThrow(() -> new SymbolDataNotFoundException("SymbolData not found"));
        SymbolDataEntity lastSymbolDataEntity = symbolDataRepository
                .findFirstByFetchedAtBetweenOrderByFetchedAtDesc(startInstant, endInstant)
                .get();

        BigDecimal lastPrice = lastSymbolDataEntity.getPrice();

        String trend;
        if (lastPrice.compareTo(averagePrice) == 0) trend = "FLAT";
        else if (lastPrice.compareTo(averagePrice) > 0) trend = "UPWARD";
        else trend = "DOWNWARD";
        return TrendDto.builder()
                .start_date(fromDate)
                .end_date(toDate)
                .symbol(lastSymbolDataEntity.getSymbol())
                .trend(trend)
                .build();
    }


    private static void validateDates(LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new IllegalDateValueException("fromDate(%s) can't be after toDate(%s)".formatted(fromDate, toDate));
        }
    }
}
