package com.health.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserSummaryResponse(
    Long userId,
    String username,
    String phone,
    String role,
    int status,
    String createTime,
    LocalDate birthDate,
    String gender,
    BigDecimal height
) {}
