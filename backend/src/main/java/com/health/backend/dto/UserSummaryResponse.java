package com.health.backend.dto;

public record UserSummaryResponse(
    Long userId,
    String username,
    String phone,
    String role
) {
}
