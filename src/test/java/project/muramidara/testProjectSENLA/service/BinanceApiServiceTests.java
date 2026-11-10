package project.muramidara.testProjectSENLA.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientResponseException;
import project.muramidara.testProjectSENLA.dto.BinanceResponseDto;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;
import project.muramidara.testProjectSENLA.http.client.BinanceClient;
import project.muramidara.testProjectSENLA.repository.SymbolDataRepository;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BinanceApiServiceTests {

    @Mock
    private BinanceClient binanceClient;
    @Mock
    private SymbolDataRepository symbolDataRepository;
    @InjectMocks
    private BinanceApiService binanceApiService;

    @BeforeEach
    public void setUp(){
        ReflectionTestUtils.setField(binanceApiService, "symbol", "BNBUSDT");
        ReflectionTestUtils.setField(binanceApiService, "defaultRetryDelayTime", "PT1M");
    }

    @Test
    @DisplayName("Test fetch and save price functionality")
    public void givenValidBinanceResponse_whenFetchAndSavePrice_thenSymbolDataIsSaved(){
        //given
        BigDecimal expectedPrice = new BigDecimal("600.25");
        long expectedCloseTime = 946684800000L;
        BinanceResponseDto responseDto = new BinanceResponseDto(
                5,
                expectedPrice,
                expectedCloseTime
        );
        given(binanceClient.getBinanceData("BNBUSDT"))
                .willReturn(responseDto);

        ArgumentCaptor<SymbolDataEntity> symbolDataCaptor =
                ArgumentCaptor.forClass(SymbolDataEntity.class);

        //when
        binanceApiService.fetchAndSavePrice();

        //then
        verify(symbolDataRepository, times(1)).save(symbolDataCaptor.capture());
        SymbolDataEntity savedSymbolData = symbolDataCaptor.getValue();
        assertThat(savedSymbolData).isNotNull();
        assertThat(savedSymbolData.getSymbol()).isEqualTo("BNBUSDT");
        assertThat(savedSymbolData.getPrice()).isEqualByComparingTo(expectedPrice);
        assertThat(savedSymbolData.getFetchedAt())
                .isEqualTo(Instant.ofEpochMilli(expectedCloseTime));
    }

    @Test
    @DisplayName("Test fetch and save price with null response functionality")
    public void givenNullBinanceResponse_whenFetchAndSavePrice_thenExceptionIsThrown(){
        //given
        given(binanceClient.getBinanceData("BNBUSDT"))
                .willReturn(null);

        //when
        assertThrows(IllegalStateException.class,
                () -> binanceApiService.fetchAndSavePrice());

        //then
        verify(symbolDataRepository, never()).save(any(SymbolDataEntity.class));
    }


    @Test
    @DisplayName("Test fetch and save price with rate limit functionality")
    public void givenRateLimitResponse_whenFetchAndSavePriceCalledAgain_thenNextRequestIsSkipped(){
        //given
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Retry-After", "60");
        RestClientResponseException exception = mock(RestClientResponseException.class);
        given(exception.getStatusCode()).willReturn(HttpStatus.TOO_MANY_REQUESTS);
        given(exception.getResponseHeaders()).willReturn(responseHeaders);
        given(binanceClient.getBinanceData("BNBUSDT"))
                .willThrow(exception);

        //when
        assertThrows(RestClientResponseException.class,
                () -> binanceApiService.fetchAndSavePrice());
        assertDoesNotThrow(() -> binanceApiService.fetchAndSavePrice());

        //then
        verify(binanceClient, times(1)).getBinanceData("BNBUSDT");
        verify(symbolDataRepository, never()).save(any(SymbolDataEntity.class));
    }

    @Test
    @DisplayName("Test fetch and save price with invalid Retry-After header functionality")
    public void givenInvalidRetryAfterHeader_whenFetchAndSavePriceCalledAgain_thenDefaultDelayIsUsed(){
        //given
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Retry-After", "invalid");
        RestClientResponseException exception = mock(RestClientResponseException.class);
        given(exception.getStatusCode()).willReturn(HttpStatus.I_AM_A_TEAPOT);
        given(exception.getResponseHeaders()).willReturn(responseHeaders);
        given(binanceClient.getBinanceData("BNBUSDT"))
                .willThrow(exception);

        //when
        assertThrows(RestClientResponseException.class,
                () -> binanceApiService.fetchAndSavePrice());
        assertDoesNotThrow(() -> binanceApiService.fetchAndSavePrice());

        //then
        verify(binanceClient, times(1)).getBinanceData("BNBUSDT");
        verify(symbolDataRepository, never()).save(any(SymbolDataEntity.class));
    }

    @Test
    @DisplayName("Test fetch and save price with non-rate-limit HTTP error functionality")
    public void givenNonRateLimitHttpError_whenFetchAndSavePriceCalledAgain_thenRequestIsNotSkipped(){
        //given
        RestClientResponseException exception = mock(RestClientResponseException.class);
        given(exception.getStatusCode()).willReturn(HttpStatus.BAD_REQUEST);

        BinanceResponseDto responseDto = new BinanceResponseDto(
                5,
                new BigDecimal("600.25"),
                946684800000L
        );
        given(binanceClient.getBinanceData("BNBUSDT"))
                .willThrow(exception)
                .willReturn(responseDto);

        //when
        assertThrows(RestClientResponseException.class,
                () -> binanceApiService.fetchAndSavePrice());
        assertDoesNotThrow(() -> binanceApiService.fetchAndSavePrice());

        //then
        verify(binanceClient, times(2)).getBinanceData("BNBUSDT");
        verify(symbolDataRepository, times(1)).save(any(SymbolDataEntity.class));
    }
}
