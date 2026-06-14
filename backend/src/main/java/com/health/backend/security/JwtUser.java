package com.health.backend.security;

public record JwtUser(
    Long userId,
    String role
) {
}
