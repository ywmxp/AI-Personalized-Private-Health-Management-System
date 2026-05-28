package com.health.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.health.backend.domain.HealthPlan;

public interface HealthPlanRepository extends JpaRepository<HealthPlan, Long> {
}
