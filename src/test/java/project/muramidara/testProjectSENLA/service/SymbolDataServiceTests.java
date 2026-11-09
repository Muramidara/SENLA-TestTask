package project.muramidara.testProjectSENLA.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import project.muramidara.testProjectSENLA.util.DataUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SymbolDataServiceTests {

    @Mock
    private SymbolDataRepository symbolDataRepository;
    @Mock
    private SymbolDataMapper symbolDataMapper;
    @Mock
    private SymbolPageDtoMapper symbolPageDtoMapper;
    @InjectMocks
    private SymbolDataService symbolDataService;

    @Test
    @DisplayName("Test find last symboldata functionality")
    public void givenSavedSymbol_whenGetLastSymbolData_thenSymbolDataDtoIsReturned(){
        //given
        Optional<SymbolDataEntity> savedSymbolData = Optional.of(DataUtils.getSymbolDataEntity1Persistent());
        SymbolDataDto expectedSymbolDataDto = mock(SymbolDataDto.class);
        BDDMockito.given(symbolDataRepository.findFirstByOrderByFetchedAtDesc()).willReturn(savedSymbolData);
        BDDMockito.given(symbolDataMapper.mapEntityToDto(any(SymbolDataEntity.class)))
                .willReturn(expectedSymbolDataDto);
        //when
        SymbolDataDto symbolDataDto = symbolDataService.getLastSymbolData();

        //then
        assertThat(symbolDataDto).isNotNull();
        assertThat(symbolDataDto.getFetchedAt()).isEqualTo(expectedSymbolDataDto.getFetchedAt());
    }

    @Test
    @DisplayName("Test find last symboldata with empty database functionality")
    public void givenNoSavedSymbols_whenGetLastSymbolData_thenExceptionIsThrown(){
        //given
        Optional<SymbolDataEntity> emptySymbolData = Optional.ofNullable(null);
        BDDMockito.given(symbolDataRepository.findFirstByOrderByFetchedAtDesc()).willReturn(emptySymbolData);

        //when
        assertThrows(SymbolDataNotFoundException.class, () -> symbolDataService.getLastSymbolData());

        //then
        verify(symbolDataRepository, times(1)).findFirstByOrderByFetchedAtDesc();
        verify(symbolDataMapper, never()).mapEntityToDto(any(SymbolDataEntity.class));
    }


    @Test
    @DisplayName("Test get average price functionality")
    public void givenSavedSymbols_whenGetAveragePrice_thenAveragePriceDtoIsReturned(){
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);

        BigDecimal expectedAveragePrice = new BigDecimal("1.50");

        BDDMockito.given(symbolDataRepository.getAveragePrice(any(Instant.class), any(Instant.class)))
                .willReturn(Optional.of(expectedAveragePrice));

        //when
        AveragePriceDto averagePriceDto = symbolDataService.getAveragePrice(fromDate, toDate);

        //then
        assertThat(averagePriceDto).isNotNull();
    }

    @Test
    @DisplayName("Test get average price with empty database functionality")
    public void givenNoSavedSymbols_whenGetAveragePrice_thenExceptionIsThrown(){
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);


        BDDMockito.given(symbolDataRepository.getAveragePrice(any(Instant.class), any(Instant.class)))
                .willReturn(Optional.empty());

        //when
        assertThrows(SymbolDataNotFoundException.class,
                () -> symbolDataService.getAveragePrice(fromDate, toDate));

        //then
        verify(symbolDataRepository, times(1)).getAveragePrice(any(Instant.class), any(Instant.class));

    }

    @Test
    @DisplayName("Test get average price with incorrect dates functionality")
    public void givenIncorrectDates_whenGetAveragePrice_thenExceptionIsThrown(){
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 31);
        LocalDate toDate = LocalDate.of(2000, 1, 1);


        //when
        assertThrows(IllegalRequestValueException.class,
                () -> symbolDataService.getAveragePrice(fromDate, toDate));

        //then
        verify(symbolDataRepository, never()).getAveragePrice(any(Instant.class), any(Instant.class));

    }


    @Test
    @DisplayName("Test get all symbols in period functionality")
    public void givenSavedSymbols_whenGetAllSymbolsInPeriod_thenSymbolPageDtoIsReturned(){
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);
        int page = 0, size = 10;



        SymbolDataEntity symbolDataEntity = DataUtils.getSymbolDataEntity1Persistent();

        Page<SymbolDataEntity> symbolDataEntityPage =
                new PageImpl<>(List.of(symbolDataEntity), PageRequest.of(page, size), 1);

        SymbolPageDto expectedSymbolPageDto = mock(SymbolPageDto.class);

        BDDMockito.given(symbolDataRepository.findAllInPeriod(
                        any(Instant.class), any(Instant.class), any(Pageable.class)))
                .willReturn(symbolDataEntityPage);

        BDDMockito.given(symbolPageDtoMapper.mapEntityPageToDtoPage(symbolDataEntityPage))
                .willReturn(expectedSymbolPageDto);

        //when
        SymbolPageDto symbolPageDto =
                symbolDataService.getAllSymbolsInPeriod(fromDate, toDate, page, size);

        //then
        assertThat(symbolPageDto).isNotNull();
        assertThat(symbolPageDto).isEqualTo(expectedSymbolPageDto);
    }

    @Test
    @DisplayName("Test get all symbols in period with maximum allowed size functionality")
    public void givenMaximumAllowedSize_whenGetAllSymbolsInPeriod_thenSymbolPageDtoIsReturned() {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);
        int page = 0;
        int size = 100;

        Pageable pageable = PageRequest.of(page, size);
        Page<SymbolDataEntity> entityPage = Page.empty(pageable);
        SymbolPageDto expectedSymbolPageDto = DataUtils.getSymbolPageDto1();

        BDDMockito.given(symbolDataRepository.findAllInPeriod(
                        any(Instant.class),
                        any(Instant.class),
                        eq(pageable)))
                .willReturn(entityPage);

        BDDMockito.given(symbolPageDtoMapper.mapEntityPageToDtoPage(entityPage))
                .willReturn(expectedSymbolPageDto);

        //when
        SymbolPageDto result = symbolDataService.getAllSymbolsInPeriod(
                fromDate, toDate, page, size
        );

        //then
        assertThat(result).isEqualTo(expectedSymbolPageDto);

        verify(symbolDataRepository).findAllInPeriod(
                any(Instant.class),
                any(Instant.class),
                eq(pageable)
        );
    }

    @Test
    @DisplayName("Test get all symbols in period with empty database functionality")
    public void givenNoSavedSymbols_whenGetAllSymbolsInPeriod_thenEmptySymbolPageDtoIsReturned(){
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);
        int page = 0, size = 10;

        Pageable pageable = PageRequest.of(page, size);

        Page<SymbolDataEntity> emptySymbolDataEntityPage = Page.empty(pageable);

        SymbolPageDto expectedSymbolPageDto = mock(SymbolPageDto.class);

        BDDMockito.given(symbolDataRepository.findAllInPeriod(
                        any(Instant.class), any(Instant.class), any(Pageable.class)))
                .willReturn(emptySymbolDataEntityPage);

        BDDMockito.given(symbolPageDtoMapper.mapEntityPageToDtoPage(emptySymbolDataEntityPage))
                .willReturn(expectedSymbolPageDto);

        //when
        SymbolPageDto symbolPageDto =
                symbolDataService.getAllSymbolsInPeriod(fromDate, toDate, page, size);

        //then
        assertThat(symbolPageDto).isNotNull();
        assertThat(symbolPageDto).isEqualTo(expectedSymbolPageDto);
        verify(symbolPageDtoMapper, times(1))
                .mapEntityPageToDtoPage(emptySymbolDataEntityPage);
    }

    @Test
    @DisplayName("Test get all symbols in period with incorrect dates functionality")
    public void givenIncorrectDates_whenGetAllSymbolsInPeriod_thenExceptionIsThrown(){
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 31);
        LocalDate toDate = LocalDate.of(2000, 1, 1);
        int page = 0, size = 10;


        //when
        assertThrows(IllegalRequestValueException.class,
                () -> symbolDataService.getAllSymbolsInPeriod(fromDate, toDate, page, size));

        //then

        verify(symbolDataRepository, never()).findAllInPeriod(any(Instant.class), any(Instant.class),
                any(Pageable.class));
        verify(symbolPageDtoMapper, never()).mapEntityPageToDtoPage(any(Page.class));
    }

    @Test
    @DisplayName("Test get all symbols in period with negative page functionality")
    public void givenNegativePage_whenGetAllSymbolsInPeriod_thenExceptionIsThrown() {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);
        int page = -1;
        int size = 10;

        //when
        IllegalRequestValueException exception = assertThrows(
                IllegalRequestValueException.class,
                () -> symbolDataService.getAllSymbolsInPeriod(fromDate, toDate, page, size)
        );

        //then
        assertThat(exception.getMessage()).isEqualTo(
                ("Page must be non negative (received page: %s) " +
                        "and size must be between 1 and 100 (received size:%s)")
                                .formatted(page, size)
        );

        verifyNoInteractions(symbolDataRepository, symbolPageDtoMapper);
    }

    @Test
    @DisplayName("Test get all symbols in period with zero size functionality")
    public void givenZeroSize_whenGetAllSymbolsInPeriod_thenExceptionIsThrown() {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);
        int page = 0;
        int size = 0;

        //when
        IllegalRequestValueException exception = assertThrows(
                IllegalRequestValueException.class,
                () -> symbolDataService.getAllSymbolsInPeriod(fromDate, toDate, page, size)
        );

        //then
        assertThat(exception.getMessage()).isEqualTo(
                ("Page must be non negative (received page: %s)" +
                        " and size must be between 1 and 100 (received size:%s)")
                        .formatted(page, size)
        );

        verifyNoInteractions(symbolDataRepository, symbolPageDtoMapper);
    }

    @Test
    @DisplayName("Test get all symbols in period with size exceeding maximum functionality")
    public void givenSizeExceedingMaximum_whenGetAllSymbolsInPeriod_thenExceptionIsThrown() {
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);
        int page = 0;
        int size = 101;

        //when
        IllegalRequestValueException exception = assertThrows(
                IllegalRequestValueException.class,
                () -> symbolDataService.getAllSymbolsInPeriod(fromDate, toDate, page, size)
        );

        //then
        assertThat(exception.getMessage()).isEqualTo(
                ("Page must be non negative (received page: %s)" +
                        " and size must be between 1 and 100 (received size:%s)")
                        .formatted(page, size)
        );

        verifyNoInteractions(symbolDataRepository, symbolPageDtoMapper);
    }


    @Test
    @DisplayName("Test get upward trend functionality")
    public void givenLastPriceHigherThanAverage_whenGetTrend_thenUpwardTrendIsReturned(){
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);

        BigDecimal averagePrice = new BigDecimal("100.00");

        SymbolDataEntity lastSymbolDataEntity = DataUtils.getSymbolDataEntity1Persistent();
        lastSymbolDataEntity.setPrice(new BigDecimal("150.00"));

        BDDMockito.given(symbolDataRepository.getAveragePrice(any(Instant.class), any(Instant.class)))
                .willReturn(Optional.of(averagePrice));

        BDDMockito.given(symbolDataRepository.findLastInPeriod(
                                any(Instant.class), any(Instant.class)))
                .willReturn(Optional.of(lastSymbolDataEntity));

        //when
        TrendDto trendDto = symbolDataService.getTrend(fromDate, toDate);

        //then
        assertThat(trendDto).isNotNull();
        assertThat(trendDto.getTrend()).isEqualTo("UPWARD");
        assertThat(trendDto.getSymbol()).isEqualTo(lastSymbolDataEntity.getSymbol());
        assertThat(trendDto.getStart_date()).isEqualTo(fromDate.toString());
        assertThat(trendDto.getEnd_date()).isEqualTo(toDate.toString());
    }

    @Test
    @DisplayName("Test get downward trend functionality")
    public void givenLastPriceLowerThanAverage_whenGetTrend_thenDownwardTrendIsReturned(){
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);

        BigDecimal averagePrice = new BigDecimal("150.00");

        SymbolDataEntity lastSymbolDataEntity = DataUtils.getSymbolDataEntity1Persistent();
        lastSymbolDataEntity.setPrice(new BigDecimal("100.00"));

        BDDMockito.given(symbolDataRepository.getAveragePrice(
                        any(Instant.class), any(Instant.class)))
                .willReturn(Optional.of(averagePrice));

        BDDMockito.given(symbolDataRepository
                        .findLastInPeriod(
                                any(Instant.class), any(Instant.class)))
                .willReturn(Optional.of(lastSymbolDataEntity));

        //when
        TrendDto trendDto = symbolDataService.getTrend(fromDate, toDate);

        //then
        assertThat(trendDto).isNotNull();
        assertThat(trendDto.getTrend()).isEqualTo("DOWNWARD");
        assertThat(trendDto.getSymbol()).isEqualTo(lastSymbolDataEntity.getSymbol());
        assertThat(trendDto.getStart_date()).isEqualTo(fromDate.toString());
        assertThat(trendDto.getEnd_date()).isEqualTo(toDate.toString());
    }

    @Test
    @DisplayName("Test get flat trend functionality")
    public void givenLastPriceEqualsAverage_whenGetTrend_thenFlatTrendIsReturned(){
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);

        BigDecimal averagePrice = new BigDecimal("100.00");

        SymbolDataEntity lastSymbolDataEntity = DataUtils.getSymbolDataEntity1Persistent();
        lastSymbolDataEntity.setPrice(new BigDecimal("100.00"));

        BDDMockito.given(symbolDataRepository.getAveragePrice(
                        any(Instant.class), any(Instant.class)))
                .willReturn(Optional.of(averagePrice));

        BDDMockito.given(symbolDataRepository
                        .findLastInPeriod(
                                any(Instant.class), any(Instant.class)))
                .willReturn(Optional.of(lastSymbolDataEntity));

        //when
        TrendDto trendDto = symbolDataService.getTrend(fromDate, toDate);

        //then
        assertThat(trendDto).isNotNull();
        assertThat(trendDto.getTrend()).isEqualTo("FLAT");
        assertThat(trendDto.getSymbol()).isEqualTo(lastSymbolDataEntity.getSymbol());
        assertThat(trendDto.getStart_date()).isEqualTo(fromDate.toString());
        assertThat(trendDto.getEnd_date()).isEqualTo(toDate.toString());
    }

    @Test
    @DisplayName("Test get trend with empty database functionality")
    public void givenNoSavedSymbols_whenGetTrend_thenExceptionIsThrown(){
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 1, 31);

        BDDMockito.given(symbolDataRepository.getAveragePrice(
                        any(Instant.class), any(Instant.class)))
                .willReturn(Optional.empty());

        //when
        assertThrows(SymbolDataNotFoundException.class,
                () -> symbolDataService.getTrend(fromDate, toDate));

        //then
        verify(symbolDataRepository, never())
                .findLastInPeriod(
                        any(Instant.class), any(Instant.class));
        verify(symbolDataRepository, times(1))
                .getAveragePrice(any(Instant.class), any(Instant.class));
    }

    @Test
    @DisplayName("Test get trend with incorrect dates functionality")
    public void givenIncorrectDates_whenGetTrend_thenExceptionIsThrown(){
        //given
        LocalDate fromDate = LocalDate.of(2000, 1, 31);
        LocalDate toDate = LocalDate.of(2000, 1, 1);

        //when
        assertThrows(IllegalRequestValueException.class,
                () -> symbolDataService.getTrend(fromDate, toDate));

        //then
        verify(symbolDataRepository, never())
                .getAveragePrice(any(Instant.class), any(Instant.class));

        verify(symbolDataRepository, never())
                .findLastInPeriod(
                        any(Instant.class), any(Instant.class));
    }



}
