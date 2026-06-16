package com.health.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.health.backend.repository.HealthDataRepository;
import com.health.backend.repository.HealthPlanRepository;
import com.health.backend.repository.HealthProfileRepository;
import com.health.backend.repository.KnowledgeRepository;
import com.health.backend.repository.KnowledgePushRepository;
import com.health.backend.repository.LoginLogRepository;
import com.health.backend.repository.ReminderRepository;
import com.health.backend.repository.UserRepository;

@SpringBootTest(
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
    }
)
class BackendApplicationTests {

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private LoginLogRepository loginLogRepository;

	@MockitoBean
	private KnowledgeRepository knowledgeRepository;

	@MockitoBean
	private HealthDataRepository healthDataRepository;

	@MockitoBean
	private HealthProfileRepository healthProfileRepository;

	@MockitoBean
	private HealthPlanRepository healthPlanRepository;

	@MockitoBean
	private KnowledgePushRepository knowledgePushRepository;

	@MockitoBean
	private ReminderRepository reminderRepository;

	@Test
	void contextLoads() {
	}

}
