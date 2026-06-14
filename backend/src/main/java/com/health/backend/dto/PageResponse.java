package com.health.backend.dto;

import java.util.List;

/**
 * 分页响应，对应前端 PageData<T>
 */
public record PageResponse<T>(
    List<T> items,
    int page,
    int size,
    long total
) {}
