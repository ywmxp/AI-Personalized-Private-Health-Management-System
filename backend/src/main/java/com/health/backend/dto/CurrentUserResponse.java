package com.health.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CurrentUserResponse(
    Long userId,
    String username,
    String phone,
    LocalDate birthDate,
    String gender,
    BigDecimal height,
    String role
) {
}
