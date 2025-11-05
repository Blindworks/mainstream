package com.mainstream.user.service.impl;

import com.mainstream.user.dto.AuthResponseDto;
import com.mainstream.user.dto.LoginDto;
import com.mainstream.user.dto.UserDto;
import com.mainstream.user.entity.User;
import com.mainstream.user.exception.InvalidCredentialsException;
import com.mainstream.user.mapper.UserMapper;
import com.mainstream.user.repository.UserRepository;
import com.mainstream.user.service.AuthService;
import com.mainstream.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponseDto authenticate(LoginDto loginDto) {
        log.debug("Authenticating user with email: {}", loginDto.getEmail());

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        UserDto userDto = userMapper.toDto(user);
        String token = jwtUtil.generateToken(userDto);
        Long expiresIn = jwtUtil.getExpirationTime();

        log.info("User authenticated successfully: {}", loginDto.getEmail());

        return AuthResponseDto.builder()
                .token(token)
                .user(userDto)
                .expiresIn(expiresIn)
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public String extractEmailFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }
}