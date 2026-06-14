package com.health.backend.service;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.health.backend.domain.Reminder;
import com.health.backend.repository.ReminderRepository;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;

    public ReminderService(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    /** 获取当前用户的提醒列表 */
    @Transactional(readOnly = true)
    public List<Reminder> list(Long userId) {
        return reminderRepository.findByUserIdOrderByReminderTimeAsc(userId);
    }

    /** 新增提醒 */
    @Transactional
    public Reminder create(Long userId, String reminderType, LocalTime reminderTime, Integer status) {
        Reminder r = new Reminder();
        r.setUserId(userId);
        r.setReminderType(reminderType);
        r.setReminderTime(reminderTime);
        r.setStatus(status != null ? status : 1);
        return reminderRepository.save(r);
    }

    /** 修改提醒 */
    @Transactional
    public Reminder update(Long reminderId, Long userId, String reminderType, LocalTime reminderTime, Integer status) {
        Reminder r = reminderRepository.findById(reminderId)
            .orElseThrow(() -> new RuntimeException("提醒不存在"));
        if (!r.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该提醒");
        }
        r.setReminderType(reminderType);
        r.setReminderTime(reminderTime);
        r.setStatus(status);
        return reminderRepository.save(r);
    }

    /** 切换提醒开关 */
    @Transactional
    public void toggleStatus(Long reminderId, Long userId, Integer status) {
        Reminder r = reminderRepository.findById(reminderId)
            .orElseThrow(() -> new RuntimeException("提醒不存在"));
        if (!r.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该提醒");
        }
        r.setStatus(status);
        reminderRepository.save(r);
    }
}
