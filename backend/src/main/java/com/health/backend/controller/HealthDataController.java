package com.health.backend.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.health.backend.domain.HealthData;
import com.health.backend.dto.ApiResponse;
import com.health.backend.dto.PageResponse;
import com.health.backend.security.JwtUser;
import com.health.backend.service.HealthDataService;

@RestController
@RequestMapping("/api/health-data")
public class HealthDataController {

    private final HealthDataService healthDataService;

    public HealthDataController(HealthDataService healthDataService) {
        this.healthDataService = healthDataService;
    }

    /** 录入健康数据 */
    @PostMapping
    public ApiResponse<Map<String, Long>> create(
        @AuthenticationPrincipal JwtUser currentUser,
        @RequestBody CreateRequest req
    ) {
        HealthData data = healthDataService.create(
            currentUser.userId(), req.dataType, req.dataValue, req.unit, req.recordTime);
        return ApiResponse.success(Map.of("dataId", data.getDataId()));
    }

    /** 查询健康数据列表 */
    @GetMapping
    public ApiResponse<PageResponse<HealthData>> query(
        @AuthenticationPrincipal JwtUser currentUser,
        @RequestParam(required = false) String dataType,
        @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(healthDataService.query(
            currentUser.userId(), dataType, startTime, endTime, page, size));
    }

    /** 批量导入健康数据 CSV */
    @PostMapping("/import")
    public ApiResponse<Map<String, Integer>> importCsv(
        @AuthenticationPrincipal JwtUser currentUser,
        @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        int importedCount = healthDataService.importCsv(currentUser.userId(), file);
        return ApiResponse.success(Map.of("importedCount", importedCount));
    }

    /** 删除健康数据 */
    @DeleteMapping("/{dataId}")
    public ApiResponse<Void> delete(
        @AuthenticationPrincipal JwtUser currentUser,
        @PathVariable Long dataId
    ) {
        healthDataService.delete(dataId, currentUser.userId());
        return ApiResponse.success(null);
    }

    /** 健康趋势 */
    @GetMapping("/trends")
    public ApiResponse<Map<String, Object>> trends(
        @AuthenticationPrincipal JwtUser currentUser,
        @RequestParam String dataType,
        @RequestParam @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @RequestParam @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        return ApiResponse.success(healthDataService.getTrends(
            currentUser.userId(), dataType, startTime, endTime));
    }

    /** 录入请求体 */
    record CreateRequest(
        String dataType,
        String dataValue,
        String unit,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime recordTime
    ) {}
}
