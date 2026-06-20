package com.health.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.health.backend.domain.HealthData;

public interface HealthDataRepository extends JpaRepository<HealthData, Long>, JpaSpecificationExecutor<HealthData> {

    Optional<HealthData> findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(
        Long userId, String dataType, LocalDateTime recordTime);

    List<HealthData> findByUserIdAndRecordTimeBetweenOrderByRecordTimeAsc(
        Long userId, LocalDateTime start, LocalDateTime end);

    @Query("""
        select date(h.recordTime), count(h)
        from HealthData h
        where h.recordTime >= :start and h.recordTime < :end
        group by date(h.recordTime)
        order by date(h.recordTime)
        """)
    List<Object[]> countDailyRecordsBetween(LocalDateTime start, LocalDateTime end);
}
