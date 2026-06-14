package com.health.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.health.backend.domain.HealthProfile;

public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {
    Optional<HealthProfile> findFirstByUserIdOrderByCreateTimeDesc(Long userId);
}
