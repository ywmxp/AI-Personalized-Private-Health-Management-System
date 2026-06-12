package com.health.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.health.backend.domain.Knowledge;

public interface KnowledgeRepository extends JpaRepository<Knowledge, Long>, JpaSpecificationExecutor<Knowledge> {

    List<Knowledge> findAll();
}
