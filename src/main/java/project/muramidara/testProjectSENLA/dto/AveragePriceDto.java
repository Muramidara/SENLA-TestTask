package project.muramidara.testProjectSENLA.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AveragePriceDto {
    BigDecimal averagePrice;
}
