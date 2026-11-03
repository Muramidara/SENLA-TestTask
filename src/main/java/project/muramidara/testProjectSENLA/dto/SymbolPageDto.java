package project.muramidara.testProjectSENLA.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SymbolPageDto {

    List<SymbolDataDto> items;
    int page;
    int size;
    long totalElements;
    boolean hasNext;

}
