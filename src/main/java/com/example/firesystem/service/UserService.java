package com.example.firesystem.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.example.firesystem.exception.ResourceNotFoundException;
import com.example.firesystem.dto.UserDto;
import com.example.firesystem.mapper.UserMapper;
import com.example.firesystem.model.User;
import com.example.firesystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserDto> getUsers() {
        return userRepository.findAll().stream().map(UserMapper::userToUserDto).toList();
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User with id " + id + " not found"));
        return UserMapper.userToUserDto(user);
    }

    public User getUserByUsername(String name) {
        User user = userRepository.findByUsername(name).orElseThrow(
                () -> new ResourceNotFoundException("User with username " + name + " not found"));
        return user;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}