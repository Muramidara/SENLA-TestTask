package project.muramidara.testProjectSENLA.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BinanceResponseDto {
    int mins;
    BigDecimal price;
    long closeTime;
}
