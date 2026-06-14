package com.health.backend.controller;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.health.backend.domain.Reminder;
import com.health.backend.dto.ApiResponse;
import com.health.backend.security.JwtUser;
import com.health.backend.service.ReminderService;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /** 查询提醒列表 */
    @GetMapping
    public ApiResponse<List<Reminder>> list(@AuthenticationPrincipal JwtUser currentUser) {
        return ApiResponse.success(reminderService.list(currentUser.userId()));
    }

    /** 新增提醒 */
    @PostMapping
    public ApiResponse<Reminder> create(
        @AuthenticationPrincipal JwtUser currentUser,
        @RequestBody ReminderRequest req
    ) {
        return ApiResponse.success(reminderService.create(
            currentUser.userId(), req.reminderType, req.reminderTime, req.status));
    }

    /** 修改提醒 */
    @PutMapping("/{reminderId}")
    public ApiResponse<Reminder> update(
        @AuthenticationPrincipal JwtUser currentUser,
        @PathVariable Long reminderId,
        @RequestBody ReminderRequest req
    ) {
        return ApiResponse.success(reminderService.update(
            reminderId, currentUser.userId(), req.reminderType, req.reminderTime, req.status));
    }

    /** 启用/关闭提醒 */
    @PatchMapping("/{reminderId}/status")
    public ApiResponse<Void> toggleStatus(
        @AuthenticationPrincipal JwtUser currentUser,
        @PathVariable Long reminderId,
        @RequestBody Map<String, Integer> body
    ) {
        reminderService.toggleStatus(reminderId, currentUser.userId(), body.get("status"));
        return ApiResponse.success(null);
    }

    record ReminderRequest(String reminderType, LocalTime reminderTime, Integer status) {}
}
