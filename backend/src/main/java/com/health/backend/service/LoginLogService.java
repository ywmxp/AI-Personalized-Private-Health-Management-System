package com.health.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.health.backend.domain.LoginLog;
import com.health.backend.repository.LoginLogRepository;

@Service
public class LoginLogService {

    private static final int LOGIN_FAILED = 0;
    private static final int LOGIN_SUCCESS = 1;

    private final LoginLogRepository loginLogRepository;

    public LoginLogService(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(Long userId, String loginIp) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);
        loginLog.setLoginIp(loginIp);
        loginLog.setLoginResult(LOGIN_SUCCESS);
        loginLogRepository.save(loginLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long userId, String loginIp, String failureReason) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);
        loginLog.setLoginIp(loginIp);
        loginLog.setLoginResult(LOGIN_FAILED);
        loginLog.setFailureReason(failureReason);
        loginLogRepository.save(loginLog);
    }
}
