package com.banking.system.service.impl;

import com.banking.system.dto.request.AuthRequest;
import com.banking.system.dto.response.ApiResponse;
import com.banking.system.entity.Role;
import com.banking.system.entity.User;
import com.banking.system.exception.DuplicateResourceException;
import com.banking.system.exception.ResourceNotFoundException;
import com.banking.system.repository.RoleRepository;
import com.banking.system.repository.UserRepository;
import com.banking.system.security.JwtUtils;
import com.banking.system.security.UserDetailsImpl;
import com.banking.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public ApiResponse.JwtResponse login(AuthRequest.Login request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(request.getUsername());
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        log.info("User '{}' logged in successfully", request.getUsername());
        return ApiResponse.JwtResponse.builder()
                .accessToken(accessToken).refreshToken(refreshToken)
                .tokenType("Bearer").expiresIn(jwtUtils.getExpirationMs())
                .userId(userDetails.getId()).username(userDetails.getUsername())
                .email(userDetails.getEmail()).roles(roles).build();
    }

    @Override
    @Transactional
    public ApiResponse.MessageResponse register(AuthRequest.Register request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        User user = User.builder()
                .username(request.getUsername()).email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName()).phoneNumber(request.getPhoneNumber()).build();
        user.setRoles(resolveRoles(request.getRoles()));
        userRepository.save(user);
        log.info("New user registered: '{}'", request.getUsername());
        return ApiResponse.MessageResponse.success("User registered successfully. You may now log in.");
    }

    @Override
    public ApiResponse.JwtResponse refreshToken(AuthRequest.RefreshToken request) {
        String token = request.getRefreshToken();
        if (!jwtUtils.validateJwtToken(token)) throw new IllegalArgumentException("Invalid or expired refresh token");
        String username = jwtUtils.getUsernameFromToken(token);
        return ApiResponse.JwtResponse.builder()
                .accessToken(jwtUtils.generateRefreshToken(username))
                .refreshToken(jwtUtils.generateRefreshToken(username))
                .tokenType("Bearer").expiresIn(jwtUtils.getExpirationMs())
                .username(username).build();
    }

    @Override
    @Transactional
    public ApiResponse.MessageResponse changePassword(String username, AuthRequest.ChangePassword request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
            throw new IllegalArgumentException("Current password is incorrect");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.MessageResponse.success("Password changed successfully");
    }

    private Set<Role> resolveRoles(Set<String> strRoles) {
        Set<Role> roles = new HashSet<>();
        if (strRoles == null || strRoles.isEmpty()) {
            roles.add(findRole(Role.ERole.ROLE_USER));
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin" -> roles.add(findRole(Role.ERole.ROLE_ADMIN));
                    case "mod"   -> roles.add(findRole(Role.ERole.ROLE_MODERATOR));
                    default      -> roles.add(findRole(Role.ERole.ROLE_USER));
                }
            });
        }
        return roles;
    }

    private Role findRole(Role.ERole eRole) {
        return roleRepository.findByName(eRole)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + eRole.name()));
    }
}