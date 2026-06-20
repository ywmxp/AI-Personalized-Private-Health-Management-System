package com.health.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.health.backend.domain.HealthProfile;

public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {
    Optional<HealthProfile> findFirstByUserIdOrderByCreateTimeDesc(Long userId);

    @Query("""
        select hp from HealthProfile hp
        where hp.createTime = (
            select max(latest.createTime) from HealthProfile latest
            where latest.userId = hp.userId
        )
        and hp.profileId = (
            select max(latest.profileId) from HealthProfile latest
            where latest.userId = hp.userId and latest.createTime = hp.createTime
        )
        """)
    List<HealthProfile> findLatestProfilesPerUser();
}
