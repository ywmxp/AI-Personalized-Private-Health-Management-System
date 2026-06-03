package com.health.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.health.backend.domain.KnowledgePush;

public interface KnowledgePushRepository extends JpaRepository<KnowledgePush, Long> {
}
