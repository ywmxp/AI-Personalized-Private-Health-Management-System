package com.health.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.health.backend.domain.KnowledgePush;

public interface KnowledgePushRepository extends JpaRepository<KnowledgePush, Long> {

    Page<KnowledgePush> findByUserIdOrderByPushTimeDesc(Long userId, Pageable pageable);

    Optional<KnowledgePush> findByPushIdAndUserId(Long pushId, Long userId);
}
