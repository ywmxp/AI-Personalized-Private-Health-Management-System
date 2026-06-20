package com.health.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.health.backend.domain.User;
import com.health.backend.domain.UserRole;
import com.health.backend.domain.UserStatus;
import com.health.backend.dto.ApiResponse;
import com.health.backend.dto.PageResponse;
import com.health.backend.dto.UserSummaryResponse;
import com.health.backend.exception.BusinessException;
import com.health.backend.exception.ErrorCode;
import com.health.backend.repository.HealthDataRepository;
import com.health.backend.repository.HealthProfileRepository;
import com.health.backend.repository.LoginLogRepository;
import com.health.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginLogRepository loginLogRepository;

    @Mock
    private HealthDataRepository healthDataRepository;

    @Mock
    private HealthProfileRepository healthProfileRepository;

    private AdminController adminController;

    @BeforeEach
    void setUp() {
        adminController = new AdminController(
            userRepository,
            loginLogRepository,
            healthDataRepository,
            healthProfileRepository
        );
    }

    @Test
    void listUsersAcceptsNumericEnabledStatus() {
        User user = buildUser(1L, "enabled-user", UserStatus.ENABLED);
        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(user)));

        ApiResponse<PageResponse<UserSummaryResponse>> response =
            adminController.listUsers(null, null, "1", 1, 10);

        assertEquals(0, response.code());
        assertEquals(1, response.data().items().size());
        assertEquals(1, response.data().items().getFirst().status());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void listUsersAcceptsNumericDisabledStatus() {
        User user = buildUser(2L, "disabled-user", UserStatus.DISABLED);
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
            .thenReturn(page);

        ApiResponse<PageResponse<UserSummaryResponse>> response =
            adminController.listUsers(null, null, "0", 1, 10);

        assertEquals(0, response.code());
        assertEquals(0, response.data().items().getFirst().status());
    }

    @Test
    void listUsersRejectsInvalidStatusValue() {
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> adminController.listUsers(null, null, "ACTIVE", 1, 10)
        );

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getCode());
        assertEquals("状态值无效，必须为 1、0、ENABLED 或 DISABLED", exception.getMessage());
    }

    private User buildUser(Long userId, String username, UserStatus status) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setPhone("13800000000");
        user.setRole(UserRole.USER);
        user.setStatus(status);
        user.setCreateTime(LocalDateTime.of(2026, 6, 20, 10, 0));
        user.setBirthDate(LocalDate.of(1998, 7, 22));
        user.setHeight(new BigDecimal("170.00"));
        return user;
    }
}
