package com.health.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
    @NotBlank String username,
    LocalDate birthDate,
    String gender,
    BigDecimal height
) {}
