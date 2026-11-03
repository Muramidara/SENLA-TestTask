package project.muramidara.testProjectSENLA.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SymbolDataRepository extends JpaRepository<SymbolDataEntity, UUID> {

    @Query("SELECT sd FROM SymbolDataEntity sd LIMIT 1")
    Optional<SymbolDataEntity> findLast();

    @Query("SELECT sd FROM SymbolDataEntity sd WHERE sd.fetchedAt BETWEEN :fromDate AND :toDate")
    Page<SymbolDataEntity> findAllInPeriod(@Param("fromDate") LocalDateTime fromDate,
                                           @Param("toDate") LocalDateTime toDate,
                                           Pageable pageable);

    @Query("SELECT AVG(sd.price) FROM SymbolDataEntity sd WHERE sd.fetchedAt BETWEEN :fromDate AND :toDate")
    Optional<BigDecimal> getAveragePrice(@Param("fromDate") LocalDateTime fromDate,
                                         @Param("toDate") LocalDateTime toDate);
}
