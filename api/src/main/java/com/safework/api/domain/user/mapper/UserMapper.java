package com.safework.api.domain.user.mapper;

import com.safework.api.domain.user.dto.UserDto;
import com.safework.api.domain.user.dto.UserProfileDto;
import com.safework.api.domain.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getOrganization().getId(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getCreatedAt()
        );
    }

    public UserProfileDto toProfileDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getOrganization().getName(),
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getCreatedAt()
        );
    }
}