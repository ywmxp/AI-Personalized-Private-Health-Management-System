package com.health.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.health.backend.domain.Reminder;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUserIdOrderByReminderTimeAsc(Long userId);
}
