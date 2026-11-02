package project.muramidara.testProjectSENLA.util;

import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

public class DataUtils {

    public static SymbolDataEntity getSymbolDataEntity1Transient(){
        return SymbolDataEntity.builder()
                .symbol("BNBUSDT")
                .price(BigDecimal.ONE)
                .fetchedAt(LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1 ))
                .build();
    }

    public static SymbolDataEntity getSymbolDataEntity2Transient(){
        return SymbolDataEntity.builder()
                .symbol("BNBUSDT")
                .price(BigDecimal.TWO)
                .fetchedAt(LocalDateTime.of(2000, Month.JANUARY, 2, 1, 1 ))
                .build();
    }

    public static SymbolDataEntity getSymbolDataEntity10Transient(){
        return SymbolDataEntity.builder()
                .symbol("BNBUSDT")
                .price(BigDecimal.TEN)
                .fetchedAt(LocalDateTime.of(2000, Month.JANUARY, 3, 1, 1 ))
                .build();
    }
}
