package com.health.backend.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "reminder")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reminder_id")
    private Long reminderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reminder_type", nullable = false, length = 30)
    private String reminderType;

    @Column(name = "reminder_time", nullable = false)
    private LocalTime reminderTime;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) createTime = now;
        if (updateTime == null) updateTime = now;
    }

    @PreUpdate
    void preUpdate() {
        updateTime = LocalDateTime.now();
    }

    public Long getReminderId() { return reminderId; }
    public void setReminderId(Long reminderId) { this.reminderId = reminderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getReminderType() { return reminderType; }
    public void setReminderType(String reminderType) { this.reminderType = reminderType; }
    public LocalTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalTime reminderTime) { this.reminderTime = reminderTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
