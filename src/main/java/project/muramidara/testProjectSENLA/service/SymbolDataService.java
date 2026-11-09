package project.muramidara.testProjectSENLA.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.muramidara.testProjectSENLA.dto.AveragePriceDto;
import project.muramidara.testProjectSENLA.dto.SymbolDataDto;
import project.muramidara.testProjectSENLA.dto.SymbolPageDto;
import project.muramidara.testProjectSENLA.dto.TrendDto;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;
import project.muramidara.testProjectSENLA.exception.IllegalRequestValueException;
import project.muramidara.testProjectSENLA.exception.SymbolDataNotFoundException;
import project.muramidara.testProjectSENLA.mapper.SymbolDataMapper;
import project.muramidara.testProjectSENLA.mapper.SymbolPageDtoMapper;
import project.muramidara.testProjectSENLA.repository.SymbolDataRepository;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

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
        Instant endInstant = toDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        return symbolDataRepository.getAveragePrice(startInstant, endInstant)
                .map(AveragePriceDto::new)
                .orElseThrow(() -> new SymbolDataNotFoundException("SymbolData not found"));
    }


    public SymbolPageDto getAllSymbolsInPeriod(LocalDate fromDate, LocalDate toDate, int page, int size) {
        validatePageParameters(page, size);
        validateDates(fromDate, toDate);
        Instant startInstant = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endInstant = toDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Pageable pageable = PageRequest.of(page, size);
        Page<SymbolDataEntity> symbolDataEntityPage = symbolDataRepository.findAllInPeriod(startInstant, endInstant,
                pageable);
        return symbolPageDtoMapper.mapEntityPageToDtoPage(symbolDataEntityPage);
    }



    public TrendDto getTrend(LocalDate fromDate, LocalDate toDate) {
        validateDates(fromDate, toDate);
        Instant startInstant = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endInstant = toDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        BigDecimal averagePrice = symbolDataRepository.getAveragePrice(startInstant, endInstant)
                .orElseThrow(() -> new SymbolDataNotFoundException("SymbolData not found"));
        SymbolDataEntity lastSymbolDataEntity = symbolDataRepository
                .findLastInPeriod(startInstant, endInstant)
                .get();

        BigDecimal lastPrice = lastSymbolDataEntity.getPrice();

        String trend;
        if (lastPrice.compareTo(averagePrice) == 0) trend = "FLAT";
        else if (lastPrice.compareTo(averagePrice) > 0) trend = "UPWARD";
        else trend = "DOWNWARD";
        return TrendDto.builder()
                .start_date(fromDate.toString())
                .end_date(toDate.toString())
                .symbol(lastSymbolDataEntity.getSymbol())
                .trend(trend)
                .build();
    }

    private void validatePageParameters(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalRequestValueException(("Page must be non negative (received page: %s)" +
                    " and size must be between 1 and 100 (received size:%s)").formatted(page, size));
        }
    }

    private static void validateDates(LocalDate fromDate, LocalDate toDate) {
        if (!fromDate.isBefore(toDate)) {
            throw new IllegalRequestValueException("to(%s) must be after from(%s)".formatted(toDate, fromDate));
        }
    }
}
