package project.muramidara.testProjectSENLA.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.muramidara.testProjectSENLA.entity.SymbolDataEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SymbolDataRepository extends JpaRepository<SymbolDataEntity, UUID> {


    Optional<SymbolDataEntity> findFirstByOrderByFetchedAtDesc();

    @Query(value = "SELECT * FROM public.symbols_data " +
            "WHERE fetched_at >= :fromDate AND fetched_at < :toDate " +
            "ORDER BY fetched_at DESC" +
            " LIMIT 1",
    nativeQuery = true)
    Optional<SymbolDataEntity> findLastInPeriod(Instant fromDate,
                                                Instant toDate);

    @Query("SELECT sd FROM SymbolDataEntity sd WHERE sd.fetchedAt >= :fromDate AND sd.fetchedAt < :toDate" +
            " ORDER BY sd.fetchedAt DESC")
    Page<SymbolDataEntity> findAllInPeriod(@Param("fromDate") Instant fromDate,
                                           @Param("toDate") Instant toDate,
                                           Pageable pageable);

    @Query("SELECT AVG(sd.price) FROM SymbolDataEntity sd WHERE sd.fetchedAt >= :fromDate AND sd.fetchedAt < :toDate")
    Optional<BigDecimal> getAveragePrice(@Param("fromDate") Instant fromDate,
                                         @Param("toDate") Instant toDate);
}
