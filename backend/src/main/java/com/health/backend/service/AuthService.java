package com.health.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.health.backend.domain.User;
import com.health.backend.domain.UserRole;
import com.health.backend.domain.UserStatus;
import com.health.backend.dto.LoginRequest;
import com.health.backend.dto.LoginResponse;
import com.health.backend.dto.RegisterRequest;
import com.health.backend.dto.RegisterResponse;
import com.health.backend.exception.BusinessException;
import com.health.backend.exception.ErrorCode;
import com.health.backend.repository.UserRepository;
import com.health.backend.security.JwtTokenProvider;

@Service
public class AuthService {

    private static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    private static final String BAD_CREDENTIALS = "BAD_CREDENTIALS";
    private static final String USER_DISABLED = "USER_DISABLED";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginLogService loginLogService;
    private final UserMapper userMapper;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider,
        LoginLogService loginLogService,
        UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginLogService = loginLogService;
        this.userMapper = userMapper;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "两次输入的密码不一致");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new BusinessException(ErrorCode.PHONE_EXISTS, "手机号已存在");
        }

        User user = new User();
        user.setPhone(request.phone());
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ENABLED);

        User savedUser = userRepository.save(user);
        return new RegisterResponse(savedUser.getUserId());
    }

    @Transactional
    public LoginResponse login(LoginRequest request, String loginIp) {
        User user = userRepository.findByPhone(request.phone()).orElse(null);
        if (user == null) {
            loginLogService.recordFailure(null, loginIp, USER_NOT_FOUND);
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "手机号或密码错误");
        }

        if (user.getStatus() == UserStatus.DISABLED) {
            loginLogService.recordFailure(user.getUserId(), loginIp, USER_DISABLED);
            throw new BusinessException(ErrorCode.USER_DISABLED, "账号已被禁用");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginLogService.recordFailure(user.getUserId(), loginIp, BAD_CREDENTIALS);
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "手机号或密码错误");
        }

        loginLogService.recordSuccess(user.getUserId(), loginIp);
        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getRole().name());
        return new LoginResponse(token, userMapper.toSummaryResponse(user));
    }
}
