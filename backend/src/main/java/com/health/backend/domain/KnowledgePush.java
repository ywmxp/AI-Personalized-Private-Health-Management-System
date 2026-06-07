package com.health.backend.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "knowledge_push")
public class KnowledgePush {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "push_id")
    private Long pushId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "knowledge_id", nullable = false)
    private Long knowledgeId;

    @Column(name = "push_time", nullable = false)
    private LocalDateTime pushTime;

    @Column(name = "is_read", nullable = false)
    private Integer isRead;

    @PrePersist
    void prePersist() {
        if (pushTime == null) {
            pushTime = LocalDateTime.now();
        }
    }

    public Long getPushId() { return pushId; }
    public void setPushId(Long pushId) { this.pushId = pushId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getKnowledgeId() { return knowledgeId; }
    public void setKnowledgeId(Long knowledgeId) { this.knowledgeId = knowledgeId; }
    public LocalDateTime getPushTime() { return pushTime; }
    public void setPushTime(LocalDateTime pushTime) { this.pushTime = pushTime; }
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
}
