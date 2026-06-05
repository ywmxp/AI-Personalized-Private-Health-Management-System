package com.health.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.health.backend.domain.Knowledge;

public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {

    List<Knowledge> findAll();
}
