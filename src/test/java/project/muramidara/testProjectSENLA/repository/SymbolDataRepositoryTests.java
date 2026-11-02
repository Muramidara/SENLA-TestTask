package project.muramidara.testProjectSENLA.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;
import project.muramidara.testProjectSENLA.util.DataUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class SymbolDataRepositoryTests {
    @Autowired
    private SymbolDataRepository repository;

    @BeforeEach
    public void setUp(){
        repository.deleteAll();
    }

    @Test
    @DisplayName("Test save SymbolData functionality")
    public void givenSymbolDataObject_whenSave_thenSymbolDataIsCreated(){
        //given
        SymbolDataEntity symbolDataToSave = DataUtils.getSymbolDataEntity1Transient();
        //when
        SymbolDataEntity savedSymbolData = repository.save(symbolDataToSave);
        //then
        assertThat(savedSymbolData).isNotNull();
        assertThat(savedSymbolData.getId()).isNotNull();
        assertThat(savedSymbolData.getSymbol()).isEqualTo(symbolDataToSave.getSymbol());
        assertThat(savedSymbolData.getPrice()).isEqualByComparingTo(symbolDataToSave.getPrice());
        assertThat(savedSymbolData.getFetchedAt()).isEqualTo(symbolDataToSave.getFetchedAt());
    }

    @Test
    @DisplayName("Test findLast SymbolData functionality")
    public void givenSymbolDataIsStored_whenFindLast_thenSymbolDataIsReturned(){
        //given
        SymbolDataEntity symbolDataToSave = DataUtils.getSymbolDataEntity1Transient();
        repository.save(symbolDataToSave);
        //when
        SymbolDataEntity obtainedSymbolData = repository.findLast().orElse(null);
        //then
        assertThat(obtainedSymbolData).isNotNull();
        assertThat(obtainedSymbolData.getId()).isNotNull();
        assertThat(obtainedSymbolData.getSymbol()).isEqualTo(symbolDataToSave.getSymbol());
        assertThat(obtainedSymbolData.getPrice()).isEqualByComparingTo(symbolDataToSave.getPrice());
        assertThat(obtainedSymbolData.getFetchedAt()).isEqualTo(symbolDataToSave.getFetchedAt());
    }

    @Test
    @DisplayName("Test findLast SymbolData not found functionality")
    public void givenSymbolDataIsNotStored_whenFindLast_thenOptionalIsEmpty(){
        //given

        //when
        SymbolDataEntity obtainedSymbolData = repository.findLast().orElse(null);
        //then
        assertThat(obtainedSymbolData).isNull();
    }

    @Test
    @DisplayName("Test findAllInPeriod SymbolData functionality")
    public void givenThreeSymbolDataAreStored_whenFindAllInPeriod_thenAllSymbolDataInPeriodAreReturned(){
        //given
        LocalDateTime fromDate = LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1 );
        LocalDateTime toDate = LocalDateTime.of(2000, Month.JANUARY, 2, 1, 1 );
        SymbolDataEntity symbolDataToSave1 = DataUtils.getSymbolDataEntity1Transient();
        SymbolDataEntity symbolDataToSave2 = DataUtils.getSymbolDataEntity2Transient();
        SymbolDataEntity symbolDataToSave3 = DataUtils.getSymbolDataEntity10Transient();
        repository.saveAll(List.of(symbolDataToSave1, symbolDataToSave2, symbolDataToSave3));

        //when
        List<SymbolDataEntity> obtainedSymbolData = repository.findAllInPeriod(fromDate, toDate);
        //then
        assertThat(obtainedSymbolData).isNotEmpty();
        assertThat(obtainedSymbolData).hasSize(2);
        assertThat(obtainedSymbolData).extracting(SymbolDataEntity::getPrice)
                .contains(BigDecimal.ONE, BigDecimal.TWO);
    }

    @Test
    @DisplayName("Test findAllInPeriod  SymbolData not found functionality")
    public void givenSymbolDataIsNotStored_whenFindAllInPeriod_thenNoSymbolDataIsReturned(){
        //given
        LocalDateTime fromDate = LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1 );
        LocalDateTime toDate = LocalDateTime.of(2000, Month.JANUARY, 2, 1, 1 );

        //when
        List<SymbolDataEntity> obtainedSymbolData = repository.findAllInPeriod(fromDate, toDate);
        //then
        assertThat(obtainedSymbolData).isEmpty();
           }

    @Test
    @DisplayName("Test getAveragePrice functionality")
    public void givenThreeSymbolDataAreStored_whenGetAveragePrice_thenAveragePriceInPeriodIsReturned(){
        //given
        LocalDateTime fromDate = LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1 );
        LocalDateTime toDate = LocalDateTime.of(2000, Month.JANUARY, 2, 1, 1 );

        BigDecimal rightAveragePrice = BigDecimal.valueOf(1.5);

        SymbolDataEntity symbolDataToSave1 = DataUtils.getSymbolDataEntity1Transient();
        SymbolDataEntity symbolDataToSave2 = DataUtils.getSymbolDataEntity2Transient();
        SymbolDataEntity symbolDataToSave3 = DataUtils.getSymbolDataEntity10Transient();
        repository.saveAll(List.of(symbolDataToSave1, symbolDataToSave2, symbolDataToSave3));


        //when
        BigDecimal obtainedAveragePrice = repository.getAveragePrice(fromDate, toDate).orElse(null);
        //then
        assertThat(obtainedAveragePrice).isNotNull();
        assertThat(obtainedAveragePrice).isEqualByComparingTo(rightAveragePrice);

    }

    @Test
    @DisplayName("Test findAllInPeriod  SymbolData not found functionality")
    public void givenSymbolDataIsNotStored_whenGetAveragePrice_thenNoPriceIsReturned(){
        //given
        LocalDateTime fromDate = LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1 );
        LocalDateTime toDate = LocalDateTime.of(2000, Month.JANUARY, 2, 1, 1 );

        //when
        BigDecimal obtainedAveragePrice = repository.getAveragePrice(fromDate, toDate).orElse(null);
        //then
        assertThat(obtainedAveragePrice).isNull();
    }

}
