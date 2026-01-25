package com.example.firesystem.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.example.firesystem.exception.ResourceNotFoundException;
import com.example.firesystem.dto.UserDto;
import com.example.firesystem.mapper.UserMapper;
import com.example.firesystem.model.User;
import com.example.firesystem.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserDto> getUsers() {
        log.info("Получение списка всех пользователей");
        List<UserDto> users = userRepository.findAll().stream()
                .map(UserMapper::userToUserDto)
                .toList();
        log.debug("Найдено {} пользователей", users.size());
        return users;
    }

    public UserDto getUserById(Long id) {
        log.info("Получение пользователя по ID: {}", id);
        User user = userRepository.findById(id).orElseThrow(
                () -> {
                    log.error("Пользователь с ID {} не найден", id);
                    return new ResourceNotFoundException("User with id " + id + " not found");
                });
        log.debug("Пользователь с ID {} найден: {}", id, user.getUsername());
        return UserMapper.userToUserDto(user);
    }

    public User getUserByUsername(String name) {
        log.info("Получение пользователя по имени пользователя: {}", name);
        User user = userRepository.findByUsername(name).orElseThrow(
                () -> {
                    log.error("Пользователь с именем {} не найден", name);
                    return new ResourceNotFoundException("User with username " + name + " not found");
                });
        log.debug("Пользователь с именем {} найден: ID={}", name, user.getId());
        return user;
    }

    @Transactional
    public User saveUser(User user) {
        log.info("Сохранение пользователя: {}", user.getUsername());
        User savedUser = userRepository.save(user);
        log.debug("Пользователь сохранен с ID: {}", savedUser.getId());
        return savedUser;
    }

    @Transactional
    public void updateTelegramId(Long userId, Long telegramChatId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        user.setTelegramChatId(telegramChatId);
        userRepository.save(user);
    }
}