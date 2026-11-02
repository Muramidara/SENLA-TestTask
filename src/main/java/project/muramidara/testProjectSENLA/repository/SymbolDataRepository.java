package project.muramidara.testProjectSENLA.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SymbolDataRepository extends JpaRepository<SymbolDataEntity, UUID> {

    @Query("SELECT sd FROM SymbolDataEntity sd LIMIT 1")
    Optional<SymbolDataEntity> findLast();

    @Query("SELECT sd FROM SymbolDataEntity sd WHERE sd.fetchedAt BETWEEN :fromDate AND :toDate")
    List<SymbolDataEntity> findAllInPeriod(@Param("fromDate") LocalDateTime fromDate,
                                                  @Param("toDate") LocalDateTime toDate);

}
