package com.health.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.health.backend.domain.HealthProfile;

public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {
}
