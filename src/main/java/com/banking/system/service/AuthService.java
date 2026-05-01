package com.banking.system.service;

import com.banking.system.dto.request.AuthRequest;
import com.banking.system.dto.response.ApiResponse;

public interface AuthService {
    ApiResponse.JwtResponse login(AuthRequest.Login request);
    ApiResponse.MessageResponse register(AuthRequest.Register request);
    ApiResponse.JwtResponse refreshToken(AuthRequest.RefreshToken request);
    ApiResponse.MessageResponse changePassword(String username, AuthRequest.ChangePassword request);
}