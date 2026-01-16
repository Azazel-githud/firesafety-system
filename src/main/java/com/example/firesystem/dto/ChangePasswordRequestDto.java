package com.example.firesystem.dto;

public record ChangePasswordRequestDto(String oldPassword,
                String newPassword,
                String newPasswordAgain) {

}