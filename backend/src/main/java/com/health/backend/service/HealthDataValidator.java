package com.health.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.health.backend.exception.BusinessException;
import com.health.backend.exception.ErrorCode;

final class HealthDataValidator {

    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^\\d+(?:\\.\\d+)?$");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern BLOOD_PRESSURE_PATTERN = Pattern.compile("^(\\d{2,3})\\s*/\\s*(\\d{2,3})$");
    private static final String SUPPORTED_TYPES =
        "BLOOD_PRESSURE,BLOOD_GLUCOSE,WEIGHT,EXERCISE_MINUTES,SLEEP_HOURS";

    private HealthDataValidator() {
    }

    static ValidatedHealthData validateForCreate(
        String dataType,
        String dataValue,
        String unit,
        LocalDateTime recordTime
    ) {
        LocalDateTime normalizedRecordTime = recordTime != null ? recordTime : LocalDateTime.now();
        return validate(dataType, dataValue, unit, normalizedRecordTime,
            message -> new BusinessException(ErrorCode.VALIDATION_ERROR, message));
    }

    static ValidatedHealthData validateForCsv(
        String dataType,
        String dataValue,
        String unit,
        LocalDateTime recordTime,
        int lineNumber
    ) {
        if (recordTime == null) {
            throw csvLineError(lineNumber, "recordTime不能为空");
        }
        return validate(dataType, dataValue, unit, recordTime,
            message -> csvLineError(lineNumber, message));
    }

    private static ValidatedHealthData validate(
        String dataType,
        String dataValue,
        String unit,
        LocalDateTime recordTime,
        ErrorFactory errorFactory
    ) {
        HealthMetricType metricType = resolveMetricType(dataType, errorFactory);
        String normalizedValue = validateValue(metricType, dataValue, errorFactory);
        String normalizedUnit = normalizeUnit(metricType, unit, errorFactory);
        validateRecordTime(recordTime, errorFactory);
        return new ValidatedHealthData(metricType.code, normalizedValue, normalizedUnit, recordTime);
    }

    private static HealthMetricType resolveMetricType(String rawDataType, ErrorFactory errorFactory) {
        if (rawDataType == null || rawDataType.isBlank()) {
            throw errorFactory.create("dataType不能为空");
        }
        String normalized = rawDataType.trim().toUpperCase(Locale.ROOT);
        for (HealthMetricType metricType : HealthMetricType.values()) {
            if (metricType.code.equals(normalized)) {
                return metricType;
            }
        }
        throw errorFactory.create("dataType不支持，必须为" + SUPPORTED_TYPES);
    }

    private static String validateValue(
        HealthMetricType metricType,
        String rawDataValue,
        ErrorFactory errorFactory
    ) {
        if (rawDataValue == null || rawDataValue.isBlank()) {
            throw errorFactory.create("dataValue不能为空");
        }
        String dataValue = rawDataValue.trim();

        return switch (metricType) {
            case BLOOD_PRESSURE -> validateBloodPressure(dataValue, errorFactory);
            case BLOOD_GLUCOSE -> validateDecimalRange(
                dataValue,
                new BigDecimal("0.1"),
                new BigDecimal("50"),
                "BLOOD_GLUCOSE必须为0.1-50之间的数字",
                errorFactory
            );
            case WEIGHT -> validateDecimalRange(
                dataValue,
                new BigDecimal("1"),
                new BigDecimal("500"),
                "WEIGHT必须为1-500之间的数字",
                errorFactory
            );
            case EXERCISE_MINUTES -> validateIntegerRange(
                dataValue,
                0,
                1440,
                "EXERCISE_MINUTES必须为0-1440之间的整数",
                errorFactory
            );
            case SLEEP_HOURS -> validateDecimalRange(
                dataValue,
                BigDecimal.ZERO,
                new BigDecimal("24"),
                "SLEEP_HOURS必须为0-24之间的数字",
                errorFactory
            );
        };
    }

    private static String validateBloodPressure(String dataValue, ErrorFactory errorFactory) {
        Matcher matcher = BLOOD_PRESSURE_PATTERN.matcher(dataValue);
        if (!matcher.matches()) {
            throw errorFactory.create("BLOOD_PRESSURE格式必须为收缩压/舒张压，例如120/80");
        }

        int systolic = Integer.parseInt(matcher.group(1));
        int diastolic = Integer.parseInt(matcher.group(2));
        if (systolic < 40 || systolic > 300) {
            throw errorFactory.create("BLOOD_PRESSURE收缩压必须在40-300之间");
        }
        if (diastolic < 30 || diastolic > 200) {
            throw errorFactory.create("BLOOD_PRESSURE舒张压必须在30-200之间");
        }
        if (systolic <= diastolic) {
            throw errorFactory.create("BLOOD_PRESSURE收缩压必须大于舒张压");
        }
        return systolic + "/" + diastolic;
    }

    private static String validateDecimalRange(
        String dataValue,
        BigDecimal min,
        BigDecimal max,
        String message,
        ErrorFactory errorFactory
    ) {
        if (!DECIMAL_PATTERN.matcher(dataValue).matches()) {
            throw errorFactory.create(message);
        }
        BigDecimal numericValue = new BigDecimal(dataValue);
        if (numericValue.compareTo(min) < 0 || numericValue.compareTo(max) > 0) {
            throw errorFactory.create(message);
        }
        return dataValue;
    }

    private static String validateIntegerRange(
        String dataValue,
        int min,
        int max,
        String message,
        ErrorFactory errorFactory
    ) {
        if (!INTEGER_PATTERN.matcher(dataValue).matches()) {
            throw errorFactory.create(message);
        }
        int numericValue = Integer.parseInt(dataValue);
        if (numericValue < min || numericValue > max) {
            throw errorFactory.create(message);
        }
        return dataValue;
    }

    private static String normalizeUnit(
        HealthMetricType metricType,
        String rawUnit,
        ErrorFactory errorFactory
    ) {
        if (rawUnit == null || rawUnit.isBlank()) {
            return metricType.defaultUnit;
        }

        String unit = rawUnit.trim();
        if (!metricType.supportsUnit(unit)) {
            throw errorFactory.create(metricType.code + "的unit必须为" + metricType.defaultUnit);
        }
        return metricType.defaultUnit;
    }

    private static void validateRecordTime(LocalDateTime recordTime, ErrorFactory errorFactory) {
        if (recordTime.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw errorFactory.create("recordTime不能晚于当前时间");
        }
    }

    private static BusinessException csvLineError(int lineNumber, String message) {
        return new BusinessException(ErrorCode.VALIDATION_ERROR, "第" + lineNumber + "行：" + message);
    }

    @FunctionalInterface
    private interface ErrorFactory {
        BusinessException create(String message);
    }

    static record ValidatedHealthData(
        String dataType,
        String dataValue,
        String unit,
        LocalDateTime recordTime
    ) {
    }

    private enum HealthMetricType {
        BLOOD_PRESSURE("BLOOD_PRESSURE", "mmHg", Set.of("mmhg")),
        BLOOD_GLUCOSE("BLOOD_GLUCOSE", "mmol/L", Set.of("mmol/l")),
        WEIGHT("WEIGHT", "kg", Set.of("kg")),
        EXERCISE_MINUTES("EXERCISE_MINUTES", "分钟", Set.of("分钟", "minutes", "minute", "mins", "min")),
        SLEEP_HOURS("SLEEP_HOURS", "小时", Set.of("小时", "hours", "hour", "h"));

        private final String code;
        private final String defaultUnit;
        private final Set<String> supportedUnits;

        HealthMetricType(String code, String defaultUnit, Set<String> supportedUnits) {
            this.code = code;
            this.defaultUnit = defaultUnit;
            this.supportedUnits = supportedUnits;
        }

        private boolean supportsUnit(String unit) {
            return supportedUnits.contains(unit.toLowerCase(Locale.ROOT));
        }
    }
}
