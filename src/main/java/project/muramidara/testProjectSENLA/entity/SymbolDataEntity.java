package project.muramidara.testProjectSENLA.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(schema = "public", name = "symbols_data")
public class SymbolDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    UUID id;

    @Column(name = "symbol",  nullable = false, length = 16)
    String symbol;

    @Column(name = "price",   nullable = false, scale = 10)
    BigDecimal price;

    @Column(name = "fetchedAt", nullable = false)
    LocalDateTime fetchedAt;

}
