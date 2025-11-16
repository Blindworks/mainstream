package com.mainstream.user.service;

import com.mainstream.user.dto.AuthResponseDto;
import com.mainstream.user.dto.ForgotPasswordRequestDto;
import com.mainstream.user.dto.LoginDto;
import com.mainstream.user.dto.MessageResponseDto;
import com.mainstream.user.dto.ResetPasswordRequestDto;

public interface AuthService {

    AuthResponseDto authenticate(LoginDto loginDto);

    boolean validateToken(String token);

    String extractEmailFromToken(String token);

    MessageResponseDto forgotPassword(ForgotPasswordRequestDto request);

    MessageResponseDto resetPassword(ResetPasswordRequestDto request);
}