package project.muramidara.testProjectSENLA.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import project.muramidara.testProjectSENLA.dto.BinanceResponseDto;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;
import project.muramidara.testProjectSENLA.http.client.BinanceClient;
import project.muramidara.testProjectSENLA.repository.SymbolDataRepository;

import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class BinanceApiService {

    private final BinanceClient binanceClient;
    private final SymbolDataRepository symbolDataRepository;
    @Value("${binance.api.symbol}")
    private String symbol;
    @Value("${binance.scheduling.default-retry-delay}")
    private String defaultRetryDelayTime;

    private Instant retryAllowedAfter = Instant.EPOCH;


    public void fetchAndSavePrice(){

        if(Instant.now().isBefore(retryAllowedAfter)){
            log.warn("Skipping Binance requests until {}", retryAllowedAfter);
            return;
        }
        BinanceResponseDto dto;
        try{
        dto = binanceClient.getBinanceData(symbol);

        } catch (RestClientResponseException e) {
            int statusCode = e.getStatusCode().value();

            if(statusCode == 429 || statusCode == 418){
                Duration retryAfter;

                HttpHeaders headers = e.getResponseHeaders();
                String retryAfterHeader = headers.getFirst("Retry-After");
                try{
                    retryAfter = Duration.ofSeconds(Long.parseLong(retryAfterHeader));
                } catch (NumberFormatException ex) {
                    log.error("Invalid Retry-After header: {}, using default retry delay time: {}",
                            retryAfterHeader, defaultRetryDelayTime);
                    retryAfter = Duration.parse(defaultRetryDelayTime);
                }

                retryAllowedAfter = Instant.now().plus(retryAfter);

                log.warn("Binance returned HTTP {}. Next request after {}",
                        statusCode,
                        retryAllowedAfter
                );
            }

            throw e;
        }

        if (dto == null) {
            throw new IllegalStateException(
                    "Binance returned an invalid response"
            );
        }

        SymbolDataEntity entity = SymbolDataEntity.builder()
                .symbol(symbol)
                .price(dto.getPrice())
                .fetchedAt(Instant.ofEpochMilli(dto.getCloseTime()))
                .build();

        symbolDataRepository.save(entity);
        log.info("Binance price successfully fetched and saved");
    }


}
