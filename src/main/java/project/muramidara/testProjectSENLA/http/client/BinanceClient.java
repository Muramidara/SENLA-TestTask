package project.muramidara.testProjectSENLA.http.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import project.muramidara.testProjectSENLA.dto.BinanceResponseDto;


import java.time.Duration;

@Component
public class BinanceClient {

    private final RestClient restClient;
    private final String endpoint;

    public BinanceClient(RestClient.Builder builder,
                         @Value("${binance.api.base-url}") String baseUrl,
                         @Value("${binance.api.api-endpoint}") String endpoint) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.endpoint = endpoint;
    }

    public BinanceResponseDto getBinanceData(String symbol) {
        return restClient.get()
                .uri(uriBuilder ->
                        uriBuilder.path(endpoint)
                                .queryParam("symbol", symbol)
                                .build())
                .retrieve()
                .body(BinanceResponseDto.class);
    }

}
