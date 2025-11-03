package com.example.firesystem.mapper;

import java.util.stream.Collectors;

import com.example.firesystem.dto.UserDto;
import com.example.firesystem.dto.UserLoggedDto;
import com.example.firesystem.model.Permission;
import com.example.firesystem.model.User;

public class UserMapper {
    public static UserDto userToUserDto(User user) {
        return new UserDto(user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole().getAuthority(),
                user.getRole().getPermissions().stream().map(Permission::getAuthority).collect(Collectors.toSet()));
    }

    public static UserLoggedDto userToUserLoggedDto(User user) {
        return new UserLoggedDto(user.getUsername(),
                user.getRole().getAuthority(),
                user.getRole().getPermissions().stream().map(Permission::getAuthority).collect(Collectors.toSet()));
    }
}
