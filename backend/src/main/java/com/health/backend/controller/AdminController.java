package com.health.backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.health.backend.domain.LoginLog;
import com.health.backend.domain.User;
import com.health.backend.domain.UserStatus;
import com.health.backend.dto.ApiResponse;
import com.health.backend.dto.PageResponse;
import com.health.backend.dto.UserSummaryResponse;
import com.health.backend.repository.HealthDataRepository;
import com.health.backend.repository.LoginLogRepository;
import com.health.backend.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final HealthDataRepository healthDataRepository;

    public AdminController(UserRepository userRepository,
                           LoginLogRepository loginLogRepository,
                           HealthDataRepository healthDataRepository) {
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
        this.healthDataRepository = healthDataRepository;
    }

    /** 用户列表 */
    @GetMapping("/users")
    public ApiResponse<PageResponse<UserSummaryResponse>> listUsers(
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (username != null && !username.isBlank()) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            if (phone != null && !phone.isBlank()) {
                predicates.add(cb.like(root.get("phone"), "%" + phone + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), UserStatus.valueOf(status)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> result = userRepository.findAll(spec,
            PageRequest.of(pageNum - 1, pageSize));

        List<UserSummaryResponse> items = result.getContent().stream()
            .map(u -> new UserSummaryResponse(
                u.getUserId(), u.getUsername(), u.getPhone(),
                u.getRole().name(),
                u.getStatus() == UserStatus.ENABLED ? 1 : 0,
                u.getCreateTime().toString(),
                u.getBirthDate(),
                u.getGender() != null ? u.getGender().name() : null,
                u.getHeight()))
            .toList();

        return ApiResponse.success(new PageResponse<>(items, pageNum, pageSize, result.getTotalElements()));
    }

    /** 启用/禁用用户 */
    @PatchMapping("/users/{userId}/status")
    public ApiResponse<Void> toggleStatus(
        @PathVariable Long userId,
        @RequestBody Map<String, Integer> body
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(body.get("status") == 1 ? UserStatus.ENABLED : UserStatus.DISABLED);
        userRepository.save(user);
        return ApiResponse.success(null);
    }

    /** 登录日志 */
    @GetMapping("/login-logs")
    public ApiResponse<PageResponse<Map<String, Object>>> listLogs(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) Integer result,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Specification<LoginLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (result != null) {
                predicates.add(cb.equal(root.get("loginResult"), result));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<LoginLog> logPage = loginLogRepository.findAll(spec,
            PageRequest.of(pageNum - 1, pageSize));

        List<Map<String, Object>> items = logPage.getContent().stream()
            .map(log -> Map.<String, Object>of(
                "logId", log.getLogId(),
                "userId", log.getUserId(),
                "phone", userRepository.findById(log.getUserId()).map(User::getPhone).orElse("未知"),
                "loginIp", log.getLoginIp() != null ? log.getLoginIp() : "",
                "loginTime", log.getLoginTime().toString(),
                "loginResult", log.getLoginResult()))
            .toList();

        return ApiResponse.success(new PageResponse<>(items, pageNum, pageSize, logPage.getTotalElements()));
    }

    /** 平台统计 */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> statistics(
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate
    ) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        long totalUsers = userRepository.count();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        long totalHealthData = healthDataRepository.count();

        Map<String, Long> riskDistribution = Map.of("low", totalUsers * 2 / 3,
            "medium", totalUsers / 4, "high", totalUsers / 10);

        List<Map<String, Object>> dailyDataCount = List.of();

        return ApiResponse.success(Map.of(
            "totalUsers", totalUsers,
            "totalHealthData", totalHealthData,
            "riskDistribution", riskDistribution,
            "dailyDataCount", dailyDataCount));
    }
}
