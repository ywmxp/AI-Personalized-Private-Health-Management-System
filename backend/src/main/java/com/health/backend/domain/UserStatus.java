package com.health.backend.domain;

import java.util.Locale;

import com.health.backend.exception.BusinessException;
import com.health.backend.exception.ErrorCode;

public enum UserStatus {
    ENABLED(1),
    DISABLED(0);

    private final int code;

    UserStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UserStatus fromRequestValue(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "状态值不能为空");
        }

        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "1", "ENABLED" -> ENABLED;
            case "0", "DISABLED" -> DISABLED;
            default -> throw new BusinessException(
                ErrorCode.VALIDATION_ERROR,
                "状态值无效，必须为 1、0、ENABLED 或 DISABLED"
            );
        };
    }

    public static UserStatus fromCode(Integer code) {
        if (code == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "状态值不能为空");
        }

        return switch (code) {
            case 1 -> ENABLED;
            case 0 -> DISABLED;
            default -> throw new BusinessException(ErrorCode.VALIDATION_ERROR, "状态值无效，必须为 1 或 0");
        };
    }
}
