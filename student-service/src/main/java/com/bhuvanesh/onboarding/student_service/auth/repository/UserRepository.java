package com.bhuvanesh.onboarding.student_service.auth.repository;

import com.bhuvanesh.onboarding.student_service.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
