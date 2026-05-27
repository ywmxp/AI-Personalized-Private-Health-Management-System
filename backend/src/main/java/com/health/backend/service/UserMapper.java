package com.health.backend.service;

import org.springframework.stereotype.Component;

import com.health.backend.domain.User;
import com.health.backend.dto.CurrentUserResponse;
import com.health.backend.dto.UserSummaryResponse;

@Component
public class UserMapper {

    public UserSummaryResponse toSummaryResponse(User user) {
        return new UserSummaryResponse(
            user.getUserId(),
            user.getUsername(),
            maskPhone(user.getPhone()),
            user.getRole().name()
        );
    }

    public CurrentUserResponse toCurrentUserResponse(User user) {
        return new CurrentUserResponse(
            user.getUserId(),
            user.getUsername(),
            maskPhone(user.getPhone()),
            user.getBirthDate(),
            user.getGender() == null ? null : user.getGender().name(),
            user.getHeight(),
            user.getRole().name()
        );
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
