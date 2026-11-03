package project.muramidara.testProjectSENLA.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TrendDto {

    String symbol;
    LocalDate start_date;
    LocalDate end_date;
    String trend;

}
