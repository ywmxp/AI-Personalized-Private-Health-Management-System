package com.health.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.health.backend.domain.LoginLog;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long>, JpaSpecificationExecutor<LoginLog> {
}
