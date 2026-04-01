package com.bhuvanesh.onboarding.student_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Configuration
 * ─────────────────────
 * Configures Swagger UI (OpenAPI 3.0) for the Student Service.
 * Accessible at: http://localhost:8080/swagger-ui/index.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Student Onboarding Service API")
                        .version("1.0.0")
                        .description("REST API for managing student records with pagination, sorting, and bulk operations.")
                        .termsOfService("https://example.com/terms")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev@example.com")
                                .url("https://example.com")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                );
    }
}
