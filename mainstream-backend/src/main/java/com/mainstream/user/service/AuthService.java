package com.mainstream.user.service;

import com.mainstream.user.dto.AuthResponseDto;
import com.mainstream.user.dto.LoginDto;

public interface AuthService {

    AuthResponseDto authenticate(LoginDto loginDto);

    boolean validateToken(String token);

    String extractEmailFromToken(String token);
}