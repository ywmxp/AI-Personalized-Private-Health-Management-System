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
@Table(name = "health_plan")
public class HealthPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_name", nullable = false, length = 50)
    private String planName;

    @Column(name = "health_need", nullable = false, length = 50)
    private String healthNeed;

    @Column(name = "diet_suggest", columnDefinition = "TEXT")
    private String dietSuggest;

    @Column(name = "sport_suggest", columnDefinition = "TEXT")
    private String sportSuggest;

    @Column(name = "sleep_suggest", columnDefinition = "TEXT")
    private String sleepSuggest;

    @Column(name = "is_valid", nullable = false)
    private Integer isValid;

    @Column(name = "feedback_status", nullable = false, length = 20)
    private String feedbackStatus;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getHealthNeed() { return healthNeed; }
    public void setHealthNeed(String healthNeed) { this.healthNeed = healthNeed; }
    public String getDietSuggest() { return dietSuggest; }
    public void setDietSuggest(String dietSuggest) { this.dietSuggest = dietSuggest; }
    public String getSportSuggest() { return sportSuggest; }
    public void setSportSuggest(String sportSuggest) { this.sportSuggest = sportSuggest; }
    public String getSleepSuggest() { return sleepSuggest; }
    public void setSleepSuggest(String sleepSuggest) { this.sleepSuggest = sleepSuggest; }
    public Integer getIsValid() { return isValid; }
    public void setIsValid(Integer isValid) { this.isValid = isValid; }
    public String getFeedbackStatus() { return feedbackStatus; }
    public void setFeedbackStatus(String feedbackStatus) { this.feedbackStatus = feedbackStatus; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
