package com.health.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.health.backend.domain.HealthData;
import com.health.backend.dto.HealthDataConflictResponse;
import com.health.backend.dto.HealthDataImportDuplicateAction;
import com.health.backend.dto.HealthDataImportResult;
import com.health.backend.exception.BusinessException;
import com.health.backend.exception.ErrorCode;
import com.health.backend.repository.HealthDataRepository;

@ExtendWith(MockitoExtension.class)
class HealthDataServiceTest {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Mock
    private HealthDataRepository healthDataRepository;

    @Test
    void createNormalizesUnitAndDefaultsRecordTime() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        when(healthDataRepository.save(any(HealthData.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(healthDataRepository.findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(any(), any(), any()))
            .thenReturn(Optional.empty());

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        HealthData created = service.create(3L, "sleep_hours", "7.5", "hours", null);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertThat(created.getUserId()).isEqualTo(3L);
        assertThat(created.getDataType()).isEqualTo("SLEEP_HOURS");
        assertThat(created.getDataValue()).isEqualTo("7.5");
        assertThat(created.getUnit()).isEqualTo("小时");
        assertThat(created.getRecordTime()).isAfterOrEqualTo(before);
        assertThat(created.getRecordTime()).isBeforeOrEqualTo(after);
        verify(healthDataRepository).save(any(HealthData.class));
    }

    @Test
    void createRejectsUnsupportedTypeWithoutSaving() {
        HealthDataService service = new HealthDataService(healthDataRepository);

        assertThatThrownBy(() -> service.create(1L, "HEART_RATE", "80", null, LocalDateTime.now()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("dataType不支持");
        verify(healthDataRepository, never()).save(any(HealthData.class));
    }

    @Test
    void createRejectsInvalidBloodPressureWithoutSaving() {
        HealthDataService service = new HealthDataService(healthDataRepository);

        assertThatThrownBy(() -> service.create(1L, "BLOOD_PRESSURE", "120", "mmHg", LocalDateTime.now()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("BLOOD_PRESSURE格式必须为收缩压/舒张压");
        verify(healthDataRepository, never()).save(any(HealthData.class));
    }

    @Test
    void createRejectsFutureRecordTimeWithoutSaving() {
        HealthDataService service = new HealthDataService(healthDataRepository);

        assertThatThrownBy(() -> service.create(
            1L,
            "WEIGHT",
            "70.5",
            "kg",
            LocalDateTime.now().plusDays(1)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("recordTime不能晚于当前时间");
        verify(healthDataRepository, never()).save(any(HealthData.class));
    }

    @Test
    void importCsvSavesRecordsAndReturnsImportedCount() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "dataType,dataValue,unit,recordTime\n"
                + "WEIGHT,70.5,kg,2026-06-01 08:30:00\n"
                + "BLOOD_PRESSURE,120/80,mmHg,2026-06-01 09:00:00\n"
        );

        when(healthDataRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(healthDataRepository.findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(any(), any(), any()))
            .thenReturn(Optional.empty());

        int count = service.importCsv(7L, file);

        assertThat(count).isEqualTo(2);
        ArgumentCaptor<List<HealthData>> captor = ArgumentCaptor.forClass(List.class);
        verify(healthDataRepository).saveAll(captor.capture());
        List<HealthData> records = captor.getValue();
        assertThat(records).hasSize(2);
        assertThat(records.get(0).getUserId()).isEqualTo(7L);
        assertThat(records.get(0).getDataType()).isEqualTo("WEIGHT");
        assertThat(records.get(0).getDataValue()).isEqualTo("70.5");
        assertThat(records.get(0).getUnit()).isEqualTo("kg");
        assertThat(records.get(0).getRecordTime()).isEqualTo(LocalDateTime.of(2026, 6, 1, 8, 30));
    }

    @Test
    void importCsvAcceptsUtf8BomHeader() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "\uFEFFdataType,dataValue,unit,recordTime\n"
                + "SLEEP_HOURS,7.5,hours,2026-06-01 07:00:00\n"
        );

        when(healthDataRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(healthDataRepository.findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(any(), any(), any()))
            .thenReturn(Optional.empty());

        int count = service.importCsv(1L, file);

        assertThat(count).isEqualTo(1);
        verify(healthDataRepository).saveAll(anyList());
    }

    @Test
    void importCsvFillsDefaultUnitWhenUnitMissing() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "dataType,dataValue,unit,recordTime\n"
                + "WEIGHT,70.5,,2026-06-01 08:30:00\n"
        );

        when(healthDataRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(healthDataRepository.findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(any(), any(), any()))
            .thenReturn(Optional.empty());

        service.importCsv(2L, file);

        ArgumentCaptor<List<HealthData>> captor = ArgumentCaptor.forClass(List.class);
        verify(healthDataRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).singleElement()
            .extracting(HealthData::getUnit)
            .isEqualTo("kg");
    }

    @Test
    void importCsvRejectsInvalidHeaderWithoutSaving() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "dataType,dataValue,recordTime\n"
                + "WEIGHT,70.5,2026-06-01 08:30:00\n"
        );

        assertThatThrownBy(() -> service.importCsv(1L, file))
            .isInstanceOf(BusinessException.class)
            .satisfies(error -> assertThat(((BusinessException) error).getCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR))
            .hasMessageContaining("CSV表头必须为dataType,dataValue,unit,recordTime");
        verify(healthDataRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsvRejectsInvalidRecordTimeWithoutSaving() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "dataType,dataValue,unit,recordTime\n"
                + "WEIGHT,70.5,kg,2026/06/01 08:30:00\n"
        );

        assertThatThrownBy(() -> service.importCsv(1L, file))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("第2行：recordTime格式必须为yyyy-MM-dd HH:mm:ss");
        verify(healthDataRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsvRejectsMissingRequiredValueWithoutSaving() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "dataType,dataValue,unit,recordTime\n"
                + "WEIGHT,,kg,2026-06-01 08:30:00\n"
        );

        assertThatThrownBy(() -> service.importCsv(1L, file))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("第2行：dataValue不能为空");
        verify(healthDataRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsvRejectsInvalidUnitWithoutSaving() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "dataType,dataValue,unit,recordTime\n"
                + "WEIGHT,70.5,mmHg,2026-06-01 08:30:00\n"
        );

        assertThatThrownBy(() -> service.importCsv(1L, file))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("第2行：WEIGHT的unit必须为kg");
        verify(healthDataRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsvRejectsInvalidDataValueWithoutSaving() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "dataType,dataValue,unit,recordTime\n"
                + "BLOOD_PRESSURE,120,mmHg,2026-06-01 08:30:00\n"
        );

        assertThatThrownBy(() -> service.importCsv(1L, file))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("第2行：BLOOD_PRESSURE格式必须为收缩压/舒张压");
        verify(healthDataRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsvRejectsFutureRecordTimeWithoutSaving() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "dataType,dataValue,unit,recordTime\n"
                + "WEIGHT,70.5,kg," + DT_FMT.format(LocalDateTime.now().plusDays(1)) + "\n"
        );

        assertThatThrownBy(() -> service.importCsv(1L, file))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("第2行：recordTime不能晚于当前时间");
        verify(healthDataRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsvRejectsEmptyFileWithoutSaving() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = new MockMultipartFile(
            "file", "empty.csv", "text/csv", new byte[0]);

        assertThatThrownBy(() -> service.importCsv(1L, file))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CSV文件不能为空");
        verify(healthDataRepository, never()).saveAll(anyList());
    }

    @Test
    void createReturnsConflictWhenDuplicateExistsWithoutOverwrite() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        LocalDateTime recordTime = LocalDateTime.of(2026, 6, 1, 8, 30);
        HealthData existing = new HealthData();
        existing.setUserId(1L);
        existing.setDataType("WEIGHT");
        existing.setDataValue("70.5");
        existing.setUnit("kg");
        existing.setRecordTime(recordTime);

        when(healthDataRepository.findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(
            1L, "WEIGHT", recordTime)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.create(1L, "WEIGHT", "69.8", "kg", recordTime, false))
            .isInstanceOf(BusinessException.class)
            .satisfies(error -> {
                BusinessException exception = (BusinessException) error;
                assertThat(exception.getCode()).isEqualTo(ErrorCode.DUPLICATE_HEALTH_DATA);
                assertThat(exception.getData()).isInstanceOf(HealthDataConflictResponse.class);
            });
        verify(healthDataRepository, never()).save(any(HealthData.class));
    }

    @Test
    void createOverwritesExistingRecordWhenConfirmed() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        LocalDateTime recordTime = LocalDateTime.of(2026, 6, 1, 8, 30);
        HealthData existing = new HealthData();
        existing.setDataId(5L);
        existing.setUserId(1L);
        existing.setDataType("WEIGHT");
        existing.setDataValue("70.5");
        existing.setUnit("kg");
        existing.setRecordTime(recordTime);

        when(healthDataRepository.findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(
            1L, "WEIGHT", recordTime)).thenReturn(Optional.of(existing));
        when(healthDataRepository.save(any(HealthData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HealthData updated = service.create(1L, "WEIGHT", "69.8", "kg", recordTime, true);

        assertThat(updated.getDataId()).isEqualTo(5L);
        assertThat(updated.getDataValue()).isEqualTo("69.8");
        verify(healthDataRepository).save(existing);
    }

    @Test
    void importCsvReturnsConflictSummaryWhenDuplicatesExistWithoutDecision() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "dataType,dataValue,unit,recordTime\n"
                + "WEIGHT,70.5,kg,2026-06-01 08:30:00\n"
        );
        HealthData existing = new HealthData();
        existing.setUserId(1L);
        existing.setDataType("WEIGHT");
        existing.setDataValue("71.0");
        existing.setUnit("kg");
        existing.setRecordTime(LocalDateTime.of(2026, 6, 1, 8, 30));

        when(healthDataRepository.findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(any(), any(), any()))
            .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.importCsv(1L, file, null))
            .isInstanceOf(BusinessException.class)
            .satisfies(error ->
                assertThat(((BusinessException) error).getCode()).isEqualTo(ErrorCode.DUPLICATE_HEALTH_DATA_IMPORT));
        verify(healthDataRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsvSkipsDuplicatesWhenRequested() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "dataType,dataValue,unit,recordTime\n"
                + "WEIGHT,70.5,kg,2026-06-01 08:30:00\n"
                + "SLEEP_HOURS,7.5,小时,2026-06-01 07:00:00\n"
        );
        HealthData existing = new HealthData();
        existing.setUserId(1L);
        existing.setDataType("WEIGHT");
        existing.setDataValue("71.0");
        existing.setUnit("kg");
        existing.setRecordTime(LocalDateTime.of(2026, 6, 1, 8, 30));

        when(healthDataRepository.findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(
            1L, "WEIGHT", LocalDateTime.of(2026, 6, 1, 8, 30))).thenReturn(Optional.of(existing));
        when(healthDataRepository.findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(
            1L, "SLEEP_HOURS", LocalDateTime.of(2026, 6, 1, 7, 0))).thenReturn(Optional.empty());
        when(healthDataRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        HealthDataImportResult result = service.importCsv(1L, file, HealthDataImportDuplicateAction.SKIP);

        assertThat(result.importedCount()).isEqualTo(1);
        assertThat(result.overwrittenCount()).isEqualTo(0);
        assertThat(result.skippedDuplicateCount()).isEqualTo(1);
    }

    @Test
    void importCsvOverwritesDuplicatesWhenRequested() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "dataType,dataValue,unit,recordTime\n"
                + "WEIGHT,70.5,kg,2026-06-01 08:30:00\n"
                + "SLEEP_HOURS,7.5,小时,2026-06-01 07:00:00\n"
        );
        HealthData existing = new HealthData();
        existing.setDataId(2L);
        existing.setUserId(1L);
        existing.setDataType("WEIGHT");
        existing.setDataValue("71.0");
        existing.setUnit("kg");
        existing.setRecordTime(LocalDateTime.of(2026, 6, 1, 8, 30));

        when(healthDataRepository.findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(
            1L, "WEIGHT", LocalDateTime.of(2026, 6, 1, 8, 30))).thenReturn(Optional.of(existing));
        when(healthDataRepository.findFirstByUserIdAndDataTypeAndRecordTimeOrderByDataIdAsc(
            1L, "SLEEP_HOURS", LocalDateTime.of(2026, 6, 1, 7, 0))).thenReturn(Optional.empty());
        when(healthDataRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        HealthDataImportResult result = service.importCsv(1L, file, HealthDataImportDuplicateAction.OVERWRITE);

        assertThat(result.importedCount()).isEqualTo(1);
        assertThat(result.overwrittenCount()).isEqualTo(1);
        assertThat(result.skippedDuplicateCount()).isEqualTo(0);
        assertThat(existing.getDataValue()).isEqualTo("70.5");
    }

    private MockMultipartFile csvFile(String content) {
        return new MockMultipartFile(
            "file",
            "health_data.csv",
            "text/csv",
            content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
