package com.health.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    String username,

    @NotNull(message = "出生日期不能为空")
    @PastOrPresent(message = "出生日期不能是未来日期")
    LocalDate birthDate,

    @NotBlank(message = "性别不能为空")
    String gender,

    @NotNull(message = "身高不能为空")
    @DecimalMin(value = "50.00", message = "身高不能低于50cm")
    @DecimalMax(value = "300.00", message = "身高不能超过300cm")
    BigDecimal height
) {
}
