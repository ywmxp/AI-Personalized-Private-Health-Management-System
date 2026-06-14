package com.health.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.health.backend.domain.HealthData;

public interface HealthDataRepository extends JpaRepository<HealthData, Long>, JpaSpecificationExecutor<HealthData> {

    List<HealthData> findByUserIdAndRecordTimeBetweenOrderByRecordTimeAsc(
        Long userId, LocalDateTime start, LocalDateTime end);
}
