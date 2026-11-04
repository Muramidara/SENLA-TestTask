package project.muramidara.testProjectSENLA.util;

import project.muramidara.testProjectSENLA.dto.AveragePriceDto;
import project.muramidara.testProjectSENLA.dto.SymbolDataDto;
import project.muramidara.testProjectSENLA.dto.SymbolPageDto;
import project.muramidara.testProjectSENLA.dto.TrendDto;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.UUID;

public class DataUtils {

    public static SymbolDataEntity getSymbolDataEntity1Transient(){
        return SymbolDataEntity.builder()
                .symbol("BNBUSDT")
                .price(BigDecimal.ONE)
                .fetchedAt(Instant.parse("2000-01-01T00:00:00Z"))
                .build();
    }

    public static SymbolDataDto getSymbolDataDto1(){
        return SymbolDataDto.builder()
                .symbol("BNBUSDT")
                .price(BigDecimal.ONE.toPlainString())
                .fetchedAt(Instant.parse("2000-01-01T00:00:00Z").toString())
                .build();
    }

    public static AveragePriceDto getAveragePriceDto1(){
        return new AveragePriceDto(BigDecimal.ONE);
    }


    public static SymbolPageDto getSymbolPageDto1() {
        return SymbolPageDto.builder()
                .items(List.of(getSymbolDataDto1()))
                .page(0)
                .size(10)
                .totalElements(1)
                .hasNext(false)
                .build();
    }

    public static TrendDto getTrendDto1() {
        return TrendDto.builder()
                .symbol("BNBUSDT")
                .start_date(LocalDate.of(2000, 1, 1))
                .end_date(LocalDate.of(2000, 1, 31))
                .trend("UPWARD")
                .build();
    }


    public static SymbolDataEntity getSymbolDataEntity1Persistent(){
        return SymbolDataEntity.builder()
                .id(UUID.randomUUID())
                .symbol("BNBUSDT")
                .price(BigDecimal.ONE)
                .fetchedAt(Instant.parse("2000-01-01T00:00:00Z"))
                .build();
    }

    public static SymbolDataEntity getSymbolDataEntity2Transient(){
        return SymbolDataEntity.builder()
                .symbol("BNBUSDT")
                .price(BigDecimal.TWO)
                .fetchedAt(Instant.parse("2000-01-02T00:00:00Z"))
                .build();
    }

    public static SymbolDataEntity getSymbolDataEntity10Transient(){
        return SymbolDataEntity.builder()
                .symbol("BNBUSDT")
                .price(BigDecimal.TEN)
                .fetchedAt(Instant.parse("2000-01-03T00:00:00Z"))
                .build();
    }
}
