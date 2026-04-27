package com.bhuvanesh.onboarding.student_service.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Exposes BCryptPasswordEncoder as a Spring bean.
 * Only spring-security-crypto is on the classpath — the full
 * Spring Security filter chain is NOT activated.
 */
@Configuration
public class PasswordConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
