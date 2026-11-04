package project.muramidara.testProjectSENLA.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TrendDto {

    String symbol;
    String start_date;
    String end_date;
    String trend;

}
