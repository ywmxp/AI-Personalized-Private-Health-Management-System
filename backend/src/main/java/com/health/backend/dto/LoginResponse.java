package com.health.backend.dto;

public record LoginResponse(
    String token,
    UserSummaryResponse user
) {
}
