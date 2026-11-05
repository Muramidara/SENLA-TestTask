package project.muramidara.testProjectSENLA.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import project.muramidara.testProjectSENLA.service.BinanceApiService;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BinanceApiSchedulerTests {

    @Mock
    private BinanceApiService binanceApiService;
    @InjectMocks
    private BinanceApiScheduler binanceApiScheduler;

    @Test
    @DisplayName("Test fetch price functionality")
    public void givenBinanceApiIsAvailable_whenFetchPrice_thenPriceIsFetchedAndSaved(){
        //given

        //when
        binanceApiScheduler.fetchPrice();

        //then
        verify(binanceApiService, times(1)).fetchAndSavePrice();
    }

    @Test
    @DisplayName("Test fetch price with Binance HTTP error functionality")
    public void givenBinanceHttpError_whenFetchPrice_thenExceptionIsHandled(){
        //given
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8
        );
        doThrow(exception).when(binanceApiService).fetchAndSavePrice();

        //when
        assertDoesNotThrow(() -> binanceApiScheduler.fetchPrice());

        //then
        verify(binanceApiService, times(1)).fetchAndSavePrice();
    }

    @Test
    @DisplayName("Test fetch price with unexpected error functionality")
    public void givenUnexpectedError_whenFetchPrice_thenExceptionIsHandled(){
        //given
        doThrow(new RuntimeException("Unexpected error"))
                .when(binanceApiService).fetchAndSavePrice();

        //when
        assertDoesNotThrow(() -> binanceApiScheduler.fetchPrice());

        //then
        verify(binanceApiService, times(1)).fetchAndSavePrice();
    }
}
