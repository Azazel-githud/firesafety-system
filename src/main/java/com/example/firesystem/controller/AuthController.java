package com.example.firesystem.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("auth/")
@AllArgsConstructor
public class AuthController {
    @GetMapping("/welcome")
    public String welcome() {
        return "This is unprotected page";
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String pageForUser() {
        return "This is page for only users";
    }

    @GetMapping("/admins")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String pageForAdmin() {
        return "This is page for only admins";
    }

    @GetMapping("/all")
    public String pageForAll() {
        return "This is page for all employees";
    }
}