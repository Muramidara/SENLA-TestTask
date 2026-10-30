package project.muramidara.testProjectSENLA.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;

import java.time.format.DateTimeFormatter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SymbolDataDto {

    String symbol;
    String price;
    String fetchedAt;

    public SymbolDataDto fromEntity(SymbolDataEntity entity) {
        return SymbolDataDto.builder().
                symbol(entity.getSymbol())
                .price(entity.getPrice().toPlainString())
                .fetchedAt(entity.getFetchedAt().format(DateTimeFormatter.ISO_INSTANT))
                .build();
    }
}
