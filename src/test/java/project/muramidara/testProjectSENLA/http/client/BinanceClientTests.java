package project.muramidara.testProjectSENLA.http.client;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import project.muramidara.testProjectSENLA.dto.BinanceResponseDto;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class BinanceClientTests {

    private MockRestServiceServer mockRestServiceServer;
    private BinanceClient binanceClient;

    @BeforeEach
    public void setUp(){
        RestClient.Builder restClientBuilder = RestClient.builder();
        mockRestServiceServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        binanceClient = new BinanceClient(
                restClientBuilder,
                "https://api.binance.com",
                "/api/v3/avgPrice"
        );
    }

    @Test
    @DisplayName("Test get Binance data functionality")
    public void givenSymbol_whenGetBinanceData_thenBinanceResponseDtoIsReturned(){
        //given
        String symbol = "BNBUSDT";
        String responseBody = """
                {
                  "mins": 5,
                  "price": "600.25",
                  "closeTime": 946684800000
                }
                """;

        mockRestServiceServer.expect(once(),
                        requestTo("https://api.binance.com/api/v3/avgPrice?symbol=BNBUSDT"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        //when
        BinanceResponseDto responseDto = binanceClient.getBinanceData(symbol);

        //then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getMins()).isEqualTo(5);
        assertThat(responseDto.getPrice()).isEqualByComparingTo(new BigDecimal("600.25"));
        assertThat(responseDto.getCloseTime()).isEqualTo(946684800000L);
        mockRestServiceServer.verify();
    }

    @Test
    @DisplayName("Test get Binance data with error response functionality")
    public void givenBinanceErrorResponse_whenGetBinanceData_thenExceptionIsThrown(){
        //given
        String symbol = "BNBUSDT";

        mockRestServiceServer.expect(once(),
                        requestTo("https://api.binance.com/api/v3/avgPrice?symbol=BNBUSDT"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        //when
        assertThrows(HttpClientErrorException.TooManyRequests.class,
                () -> binanceClient.getBinanceData(symbol));

        //then
        mockRestServiceServer.verify();
    }
}
