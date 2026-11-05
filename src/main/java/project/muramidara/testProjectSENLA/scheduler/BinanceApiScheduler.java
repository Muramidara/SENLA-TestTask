package project.muramidara.testProjectSENLA.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import project.muramidara.testProjectSENLA.service.BinanceApiService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "binance.api.scheduling",
        name = "enabled",
        havingValue = "true"
)
public class BinanceApiScheduler {

    private final BinanceApiService binanceApiService;

    @Scheduled(fixedDelayString = "${binance.api.scheduling.fixed-delay}",
            initialDelayString = "${binance.api.scheduling.initial-delay}")
    public void fetchPrice() {
        try{
            binanceApiService.fetchAndSavePrice();
            log.info("Binance price successfully fetched and saved");
        } catch (RestClientResponseException e) {
            log.error(
                    "Binance returned HTTP {}: {}",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            log.error("Failed to fetch and save Binance price", e);
        }
    }

}
