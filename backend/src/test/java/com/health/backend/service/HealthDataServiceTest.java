package com.health.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.health.backend.domain.HealthData;
import com.health.backend.exception.BusinessException;
import com.health.backend.exception.ErrorCode;
import com.health.backend.repository.HealthDataRepository;

@ExtendWith(MockitoExtension.class)
class HealthDataServiceTest {

    @Mock
    private HealthDataRepository healthDataRepository;

    @Test
    void importCsvSavesRecordsAndReturnsImportedCount() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = csvFile(
            "dataType,dataValue,unit,recordTime\n"
                + "WEIGHT,70.5,kg,2026-06-01 08:30:00\n"
                + "BLOOD_PRESSURE,120/80,mmHg,2026-06-01 09:00:00\n"
        );

        when(healthDataRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

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

        int count = service.importCsv(1L, file);

        assertThat(count).isEqualTo(1);
        verify(healthDataRepository).saveAll(anyList());
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
    void importCsvRejectsEmptyFileWithoutSaving() {
        HealthDataService service = new HealthDataService(healthDataRepository);
        MockMultipartFile file = new MockMultipartFile(
            "file", "empty.csv", "text/csv", new byte[0]);

        assertThatThrownBy(() -> service.importCsv(1L, file))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CSV文件不能为空");
        verify(healthDataRepository, never()).saveAll(anyList());
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
