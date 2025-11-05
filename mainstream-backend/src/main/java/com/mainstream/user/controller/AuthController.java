package com.mainstream.user.controller;

import com.mainstream.user.dto.AuthResponseDto;
import com.mainstream.user.dto.LoginDto;
import com.mainstream.user.dto.UserDto;
import com.mainstream.user.dto.UserRegistrationDto;
import com.mainstream.user.service.AuthService;
import com.mainstream.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        log.debug("Registering new user with email: {}", registrationDto.getEmail());
        
        UserDto newUser = userService.registerUser(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        log.debug("User login attempt for email: {}", loginDto.getEmail());
        
        AuthResponseDto response = authService.authenticate(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/user")
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader("X-User-Email") String email) {
        log.debug("Getting current user info for: {}", email);
        
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}