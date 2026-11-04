package project.muramidara.testProjectSENLA.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;
import project.muramidara.testProjectSENLA.util.DataUtils;

import java.math.BigDecimal;
import java.time.Instant;
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
    @DisplayName("Test findFirstByOrderByFetchedAtDesc SymbolData functionality")
    public void givenSymbolDataIsStored_whenFindFirstByOrderByFetchedAtDesc_thenSymbolDataIsReturned(){
        //given
        SymbolDataEntity symbolDataToSave = DataUtils.getSymbolDataEntity1Transient();
        repository.save(symbolDataToSave);
        //when
        SymbolDataEntity obtainedSymbolData = repository.findFirstByOrderByFetchedAtDesc().orElse(null);
        //then
        assertThat(obtainedSymbolData).isNotNull();
        assertThat(obtainedSymbolData.getId()).isNotNull();
        assertThat(obtainedSymbolData.getSymbol()).isEqualTo(symbolDataToSave.getSymbol());
        assertThat(obtainedSymbolData.getPrice()).isEqualByComparingTo(symbolDataToSave.getPrice());
        assertThat(obtainedSymbolData.getFetchedAt()).isEqualTo(symbolDataToSave.getFetchedAt());
    }

    @Test
    @DisplayName("Test findFirstByOrderByFetchedAtDesc SymbolData not found functionality")
    public void givenSymbolDataIsNotStored_whenFindFirstByOrderByFetchedAtDesc_thenOptionalIsEmpty(){
        //given

        //when
        SymbolDataEntity obtainedSymbolData = repository.findFirstByOrderByFetchedAtDesc().orElse(null);
        //then
        assertThat(obtainedSymbolData).isNull();
    }

    @Test
    @DisplayName("Test findFirstByFetchedAtBetweenOrderByFetchedAtDesc SymbolData functionality")
    public void givenThreeSymbolDataAreStored_whenFindFirstByFetchedAtBetweenOrderByFetchedAtDesc_thenLastSymbolDataIsReturned() {
        //given
        Instant fromDate = Instant.parse("2000-01-01T00:00:00Z");
        Instant toDate = Instant.parse("2000-01-02T00:00:00Z");

        SymbolDataEntity symbolDataToSave1 = DataUtils.getSymbolDataEntity1Transient();
        SymbolDataEntity symbolDataToSave2 = DataUtils.getSymbolDataEntity2Transient();
        SymbolDataEntity symbolDataToSave3 = DataUtils.getSymbolDataEntity10Transient();

        repository.saveAll(List.of(
                symbolDataToSave1,
                symbolDataToSave2,
                symbolDataToSave3
        ));

        //when
        SymbolDataEntity obtainedSymbolData = repository
                .findFirstByFetchedAtBetweenOrderByFetchedAtDesc(fromDate, toDate)
                .orElse(null);

        //then
        assertThat(obtainedSymbolData).isNotNull();
        assertThat(obtainedSymbolData.getId()).isNotNull();
        assertThat(obtainedSymbolData.getSymbol()).isEqualTo(symbolDataToSave2.getSymbol());
        assertThat(obtainedSymbolData.getPrice()).isEqualByComparingTo(symbolDataToSave2.getPrice());
        assertThat(obtainedSymbolData.getFetchedAt()).isEqualTo(symbolDataToSave2.getFetchedAt());
    }

    @Test
    @DisplayName("Test findFirstByFetchedAtBetweenOrderByFetchedAtDesc SymbolData not found functionality")
    public void givenSymbolDataIsNotStored_whenFindFirstByFetchedAtBetweenOrderByFetchedAtDesc_thenOptionalIsEmpty() {
        //given
        Instant fromDate = Instant.parse("2000-01-01T00:00:00Z");
        Instant toDate = Instant.parse("2000-01-02T00:00:00Z");

        //when
        SymbolDataEntity obtainedSymbolData = repository
                .findFirstByFetchedAtBetweenOrderByFetchedAtDesc(fromDate, toDate)
                .orElse(null);

        //then
        assertThat(obtainedSymbolData).isNull();
    }


    @Test
    @DisplayName("Test findAllInPeriod SymbolData functionality")
    public void givenThreeSymbolDataAreStored_whenFindAllInPeriod_thenNotEmptyPageIsReturned(){
        //given
        Instant fromDate = Instant.parse("2000-01-01T00:00:00Z");
        Instant toDate = Instant.parse("2000-01-02T00:00:00Z");
        Pageable pageable = PageRequest.of(0, 5);


        SymbolDataEntity symbolDataToSave1 = DataUtils.getSymbolDataEntity1Transient();
        SymbolDataEntity symbolDataToSave2 = DataUtils.getSymbolDataEntity2Transient();
        SymbolDataEntity symbolDataToSave3 = DataUtils.getSymbolDataEntity10Transient();
        repository.saveAll(List.of(symbolDataToSave1, symbolDataToSave2, symbolDataToSave3));

        //when
        Page<SymbolDataEntity> obtainedPage = repository.findAllInPeriod(fromDate, toDate, pageable);
        //then
        assertThat(obtainedPage).isNotEmpty();
        assertThat(obtainedPage.getTotalElements()).isEqualTo(2);
        assertThat(obtainedPage).extracting(SymbolDataEntity::getPrice)
                .contains(BigDecimal.ONE, BigDecimal.TWO);
    }

    @Test
    @DisplayName("Test findAllInPeriod  SymbolData not found functionality")
    public void givenSymbolDataIsNotStored_whenFindAllInPeriod_thenEmptyPageIsReturned(){
        //given
        Instant fromDate = Instant.parse("2000-01-01T00:00:00Z");
        Instant toDate = Instant.parse("2000-01-02T00:00:00Z");
        Pageable pageable = PageRequest.of(0, 5);


        //when
        Page<SymbolDataEntity> obtainedPage = repository.findAllInPeriod(fromDate, toDate, pageable);
        //then
        assertThat(obtainedPage).isEmpty();
           }

    @Test
    @DisplayName("Test getAveragePrice functionality")
    public void givenThreeSymbolDataAreStored_whenGetAveragePrice_thenAveragePriceInPeriodIsReturned(){
        //given
        Instant fromDate = Instant.parse("2000-01-01T00:00:00Z");
        Instant toDate = Instant.parse("2000-01-02T00:00:00Z");

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
        Instant fromDate = Instant.parse("2000-01-01T00:00:00Z");
        Instant toDate = Instant.parse("2000-01-02T00:00:00Z");

        //when
        BigDecimal obtainedAveragePrice = repository.getAveragePrice(fromDate, toDate).orElse(null);
        //then
        assertThat(obtainedAveragePrice).isNull();
    }

}
