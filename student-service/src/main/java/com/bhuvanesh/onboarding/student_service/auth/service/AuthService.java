package com.bhuvanesh.onboarding.student_service.auth.service;

import com.bhuvanesh.onboarding.student_service.auth.dto.AuthResponse;
import com.bhuvanesh.onboarding.student_service.auth.dto.LoginRequest;
import com.bhuvanesh.onboarding.student_service.auth.dto.SignupRequest;
import com.bhuvanesh.onboarding.student_service.auth.entity.User;
import com.bhuvanesh.onboarding.student_service.auth.exception.DuplicateEmailException;
import com.bhuvanesh.onboarding.student_service.auth.exception.InvalidCredentialsException;
import com.bhuvanesh.onboarding.student_service.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // ─── Signup ──────────────────────────────────────────────────────────────
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException(
                    "Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", request.getEmail());

        return AuthResponse.builder()
                .success(true)
                .message("Account created successfully")
                .user(AuthResponse.UserInfo.builder()
                        .name(user.getName())
                        .email(user.getEmail())
                        .build())
                .build();
    }

    // ─── Login ───────────────────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("User logged in: {}", request.getEmail());

        return AuthResponse.builder()
                .success(true)
                .message("Login successful")
                .user(AuthResponse.UserInfo.builder()
                        .name(user.getName())
                        .email(user.getEmail())
                        .build())
                .build();
    }
}
