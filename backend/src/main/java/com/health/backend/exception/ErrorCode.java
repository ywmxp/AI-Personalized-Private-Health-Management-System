package com.health.backend.exception;

public final class ErrorCode {

    public static final int VALIDATION_ERROR = 40000;
    public static final int UNAUTHORIZED = 40100;
    public static final int FORBIDDEN = 40300;
    public static final int PHONE_EXISTS = 40900;
    public static final int DUPLICATE_HEALTH_DATA = 40901;
    public static final int DUPLICATE_HEALTH_DATA_IMPORT = 40902;
    public static final int LOGIN_FAILED = 40101;
    public static final int USER_DISABLED = 40301;
    public static final int SERVER_ERROR = 50000;

    private ErrorCode() {
    }
}
