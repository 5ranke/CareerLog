package com.team03.careerlog.auth.dto;

import com.team03.careerlog.user.User;

import java.time.LocalDateTime;

public record UserResponse(Long id, String loginId, LocalDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getLoginId(), user.getCreatedAt());
    }
}
