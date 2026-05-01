package com.banking.system.service;

import com.banking.system.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    ApiResponse.UserProfileResponse getMyProfile(String username);
    ApiResponse.UserProfileResponse updateProfile(String username, UserUpdateRequest request);
    ApiResponse.PagedResponse<ApiResponse.UserProfileResponse> getAllUsers(Pageable pageable);
    ApiResponse.UserProfileResponse getUserById(Long id);
    ApiResponse.MessageResponse toggleUserStatus(Long id);
    ApiResponse.MessageResponse deleteUser(Long id);

    record UserUpdateRequest(String fullName, String phoneNumber, String email) {}
}