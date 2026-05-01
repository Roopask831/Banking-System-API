package com.banking.system.service.impl;

import com.banking.system.dto.response.ApiResponse;
import com.banking.system.entity.User;
import com.banking.system.exception.DuplicateResourceException;
import com.banking.system.exception.ResourceNotFoundException;
import com.banking.system.repository.UserRepository;
import com.banking.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse.UserProfileResponse getMyProfile(String username) {
        return mapToProfile(findByUsername(username));
    }

    @Override
    @Transactional
    public ApiResponse.UserProfileResponse updateProfile(String username, UserUpdateRequest request) {
        User user = findByUsername(username);
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email()))
                throw new DuplicateResourceException("Email '" + request.email() + "' is already in use");
            user.setEmail(request.email());
        }
        if (request.fullName() != null)    user.setFullName(request.fullName());
        if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber());
        userRepository.save(user);
        return mapToProfile(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse.PagedResponse<ApiResponse.UserProfileResponse> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return ApiResponse.PagedResponse.<ApiResponse.UserProfileResponse>builder()
                .content(page.getContent().stream().map(this::mapToProfile).collect(Collectors.toList()))
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).last(page.isLast()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse.UserProfileResponse getUserById(Long id) {
        return mapToProfile(findById(id));
    }

    @Override
    @Transactional
    public ApiResponse.MessageResponse toggleUserStatus(Long id) {
        User user = findById(id);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        String status = user.isEnabled() ? "enabled" : "disabled";
        return ApiResponse.MessageResponse.success("User account has been " + status);
    }

    @Override
    @Transactional
    public ApiResponse.MessageResponse deleteUser(Long id) {
        User user = findById(id);
        userRepository.delete(user);
        return ApiResponse.MessageResponse.success("User deleted successfully");
    }

    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private ApiResponse.UserProfileResponse mapToProfile(User user) {
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name()).collect(Collectors.toList());
        List<ApiResponse.AccountSummary> accounts = user.getAccounts().stream()
                .map(a -> ApiResponse.AccountSummary.builder()
                        .accountNumber(a.getAccountNumber()).accountType(a.getAccountType())
                        .balance(a.getBalance()).currency(a.getCurrency()).status(a.getStatus()).build())
                .collect(Collectors.toList());
        return ApiResponse.UserProfileResponse.builder()
                .id(user.getId()).username(user.getUsername()).email(user.getEmail())
                .fullName(user.getFullName()).phoneNumber(user.getPhoneNumber())
                .enabled(user.isEnabled()).roles(roles).accounts(accounts)
                .createdAt(user.getCreatedAt()).build();
    }
}